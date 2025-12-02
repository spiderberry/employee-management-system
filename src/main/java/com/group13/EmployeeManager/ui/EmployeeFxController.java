package com.group13.EmployeeManager.ui;

import com.group13.EmployeeManager.entity.Employee;
import com.group13.EmployeeManager.service.EmployeeService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class EmployeeFxController {

    private final EmployeeService employeeService;
    private final ObservableList<Employee> employees = FXCollections.observableArrayList();

    @FXML
    private ComboBox<SearchMode> searchModeBox;
    @FXML
    private TextField searchInput;
    @FXML
    private TableView<Employee> employeeTable;
    @FXML
    private TableColumn<Employee, Long> idColumn;
    @FXML
    private TableColumn<Employee, String> nameColumn;
    @FXML
    private TableColumn<Employee, String> emailColumn;
    @FXML
    private TableColumn<Employee, String> ssnColumn;
    @FXML
    private TableColumn<Employee, LocalDate> hireDateColumn;
    @FXML
    private TableColumn<Employee, Double> salaryColumn;
    @FXML
    private TableColumn<Employee, String> jobColumn;
    @FXML
    private TableColumn<Employee, String> divisionColumn;

    @FXML
    private TextField idField;
    @FXML
    private TextField nameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField ssnField;
    @FXML
    private TextField salaryField;
    @FXML
    private TextField jobField;
    @FXML
    private TextField divisionField;
    @FXML
    private DatePicker hireDatePicker;
    @FXML
    private Label statusLabel;

    public EmployeeFxController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @FXML
    public void initialize() {
        searchModeBox.getItems().setAll(SearchMode.values());
        searchModeBox.getSelectionModel().select(SearchMode.NAME);

        idColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getId()));
        nameColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getName()));
        emailColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getEmail()));
        ssnColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getSocialSecurityNumber()));
        hireDateColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getHireDate()));
        salaryColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getSalary()));
        jobColumn.setCellValueFactory(cell -> {
            var job = cell.getValue().getJobTitle();
            return new SimpleObjectProperty<>(job != null ? job.getTitle() : "");
        });
        divisionColumn.setCellValueFactory(cell -> {
            var division = cell.getValue().getDivision();
            return new SimpleObjectProperty<>(division != null ? division.getName() : "");
        });

        employeeTable.setItems(employees);
        employeeTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> populateForm(newSelection));

        refreshTable();
    }

    @FXML
    private void handleSearch() {
        String term = searchInput.getText() == null ? "" : searchInput.getText().trim();
        SearchMode mode = Optional.ofNullable(searchModeBox.getValue()).orElse(SearchMode.NAME);

        if (term.isBlank()) {
            refreshTable();
            return;
        }

        List<Employee> results = new ArrayList<>();
        try {
            switch (mode) {
                case ID -> {
                    Long id = parseId(term);
                    if (id != null) {
                        results.add(employeeService.findEmployeeById(id));
                    }
                }
                case NAME -> {
                    Employee employee = employeeService.findEmployeeByName(term);
                    if (employee != null) {
                        results.add(employee);
                    }
                }
                case SSN -> {
                    Employee employee = employeeService.findEmployeeBySocialSecurityNumber(term);
                    if (employee != null) {
                        results.add(employee);
                    }
                }
            }

            if (results.isEmpty()) {
                employees.clear();
                statusLabel.setText("No employee found for \"" + term + "\"");
                showInfo("No results", "No employee matched your search.");
            } else {
                employees.setAll(results);
                statusLabel.setText("Showing " + results.size() + " result(s) for \"" + term + "\"");
                employeeTable.getSelectionModel().selectFirst();
            }
        } catch (RuntimeException ex) {
            showError("Search failed", ex.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        refreshTable();
    }

    @FXML
    private void handleAdd() {
        try {
            Employee employee = new Employee();
            FormData formData = applyForm(employee);
            Employee saved = employeeService.updateEmployee(employee);
            applyJobAndDivision(saved, formData);
            refreshTable();
            selectEmployee(saved.getId());
            statusLabel.setText("Added employee #" + saved.getId());
        } catch (IllegalArgumentException ex) {
            showError("Validation error", ex.getMessage());
        } catch (RuntimeException ex) {
            showError("Unable to add employee", ex.getMessage());
        }
    }

    @FXML
    private void handleUpdate() {
        Employee current = resolveEmployeeForUpdate();
        if (current == null) {
            return;
        }

        try {
            FormData formData = applyForm(current);
            Employee saved = employeeService.updateEmployee(current);
            applyJobAndDivision(saved, formData);
            refreshTable();
            selectEmployee(saved.getId());
            statusLabel.setText("Updated employee #" + saved.getId());
        } catch (IllegalArgumentException ex) {
            showError("Validation error", ex.getMessage());
        } catch (RuntimeException ex) {
            showError("Unable to update employee", ex.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        Long id = parseId(idField.getText());
        if (id == null) {
            Employee selected = employeeTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                id = selected.getId();
            }
        }

        if (id == null) {
            showError("Delete employee", "Select an employee or enter an ID to delete.");
            return;
        }

        try {
            employeeService.deleteEmployee(id);
            refreshTable();
            clearForm();
            statusLabel.setText("Deleted employee #" + id);
        } catch (RuntimeException ex) {
            showError("Unable to delete employee", ex.getMessage());
        }
    }

    @FXML
    private void handleClearForm() {
        clearForm();
    }

    @FXML
    private void handleClearSearch() {
        searchInput.clear();
        refreshTable();
    }

    private void refreshTable() {
        employees.setAll(employeeService.findAllEmployees());
        statusLabel.setText("Loaded " + employees.size() + " employees.");
    }

    private void populateForm(Employee employee) {
        if (employee == null) {
            clearForm();
            return;
        }

        idField.setText(employee.getId() != null ? employee.getId().toString() : "");
        nameField.setText(employee.getName());
        emailField.setText(employee.getEmail());
        ssnField.setText(employee.getSocialSecurityNumber());
        salaryField.setText(employee.getSalary() == 0 ? "" : Double.toString(employee.getSalary()));
        hireDatePicker.setValue(employee.getHireDate());
        jobField.setText(employee.getJobTitle() != null ? employee.getJobTitle().getTitle() : "");
        divisionField.setText(employee.getDivision() != null ? employee.getDivision().getName() : "");
    }

    private void clearForm() {
        idField.clear();
        nameField.clear();
        emailField.clear();
        ssnField.clear();
        salaryField.clear();
        hireDatePicker.setValue(null);
        jobField.clear();
        divisionField.clear();
        employeeTable.getSelectionModel().clearSelection();
    }

    private FormData applyForm(Employee employee) {
        String name = nameField.getText() != null ? nameField.getText().trim() : "";
        if (name.isBlank()) {
            throw new IllegalArgumentException("Name is required.");
        }
        employee.setName(name);

        String email = emailField.getText() != null ? emailField.getText().trim() : "";
        employee.setEmail(email.isBlank() ? null : email);

        String ssn = ssnField.getText() != null ? ssnField.getText().trim() : "";
        employee.setSocialSecurityNumber(ssn.isBlank() ? null : ssn);

        if (salaryField.getText() != null && !salaryField.getText().trim().isEmpty()) {
            try {
                employee.setSalary(Double.parseDouble(salaryField.getText().trim()));
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Salary must be a valid number.");
            }
        }

        employee.setHireDate(hireDatePicker.getValue());

        FormData formData = new FormData();
        formData.jobTitle = jobField.getText() != null && !jobField.getText().trim().isEmpty()
                ? jobField.getText().trim()
                : null;
        formData.divisionName = divisionField.getText() != null && !divisionField.getText().trim().isEmpty()
                ? divisionField.getText().trim()
                : null;
        formData.hireDate = employee.getHireDate();
        return formData;
    }

    private Employee resolveEmployeeForUpdate() {
        Employee selected = employeeTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            return employeeService.findEmployeeById(selected.getId());
        }

        Long id = parseId(idField.getText());
        if (id == null) {
            showError("Update employee", "Select a row or enter a valid employee ID to update.");
            return null;
        }

        try {
            return employeeService.findEmployeeById(id);
        } catch (RuntimeException ex) {
            showError("Update employee", ex.getMessage());
            return null;
        }
    }

    private Long parseId(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(input.trim());
        } catch (NumberFormatException ex) {
            showError("Invalid ID", "Employee ID must be a whole number.");
            return null;
        }
    }

    private void selectEmployee(Long id) {
        if (id == null) {
            return;
        }
        employees.stream()
                .filter(emp -> id.equals(emp.getId()))
                .findFirst()
                .ifPresent(emp -> employeeTable.getSelectionModel().select(emp));
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void applyJobAndDivision(Employee employee, FormData formData) {
        boolean hireDateNeedsRestore = formData.hireDate != null;

        if (formData.jobTitle != null) {
            employeeService.assignJobToEmployee(employee, formData.jobTitle);
            hireDateNeedsRestore = true; // assign method overrides hire date
        }
        if (formData.divisionName != null) {
            employeeService.assignDivisionToEmployee(employee, formData.divisionName);
        }

        if (hireDateNeedsRestore) {
            employee.setHireDate(formData.hireDate);
            employeeService.updateEmployee(employee);
        }
    }

    private static class FormData {
        String jobTitle;
        String divisionName;
        LocalDate hireDate;
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public enum SearchMode {
        NAME("Name"),
        SSN("SSN"),
        ID("Employee ID");

        private final String label;

        SearchMode(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
