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
import javafx.scene.control.TextArea;
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
    private TextField minSalaryRangeField;
    @FXML
    private TextField maxSalaryRangeField;
    @FXML
    private TextField percentIncreaseField;
    @FXML
    private DatePicker hireDatePicker;
    @FXML
    private Label statusLabel;
    @FXML
    private ComboBox<ReportType> reportSelector;
    @FXML
    private TextArea reportOutput;

    public EmployeeFxController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @FXML
    public void initialize() {
        searchModeBox.getItems().setAll(SearchMode.values());
        searchModeBox.getSelectionModel().select(SearchMode.NAME);
        if (reportSelector != null) {
            reportSelector.getItems().setAll(ReportType.values());
            reportSelector.getSelectionModel().select(ReportType.FULL_EMPLOYEE_PAY);
        }

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

    @FXML
    private void handleApplySalaryIncrease() {
        Double min = parseDouble(minSalaryRangeField, "Minimum salary");
        Double max = parseDouble(maxSalaryRangeField, "Maximum salary");
        Double percent = parseDouble(percentIncreaseField, "Percent increase");

        if (min == null || max == null || percent == null) {
            return;
        }
        if (min < 0 || max <= 0 || max <= min) {
            showError("Salary range", "Enter a valid salary range where max is greater than min.");
            return;
        }

        List<Employee> all = employeeService.findAllEmployees();
        int updated = 0;
        for (Employee e : all) {
            double salary = e.getSalary();
            if (salary >= min && salary < max) {
                double updatedSalary = salary + (salary * (percent / 100.0));
                e.setSalary(updatedSalary);
                employeeService.updateEmployee(e);
                updated++;
            }
        }
        refreshTable();
        statusLabel.setText("Applied " + percent + "% increase to " + updated + " employee(s) in range.");
        if (updated == 0) {
            showInfo("No employees updated", "No employees matched the specified salary range.");
        }
    }

    @FXML
    private void handleRunReport() {
        if (reportSelector == null || reportOutput == null) {
            return;
        }
        ReportType type = Optional.ofNullable(reportSelector.getValue()).orElse(ReportType.FULL_EMPLOYEE_PAY);
        List<Employee> all = employeeService.findAllEmployees();
        String result = switch (type) {
            case FULL_EMPLOYEE_PAY -> buildFullEmployeePayReport(all);
            case TOTAL_PAY_BY_JOB -> buildTotalPayByJobReport(all);
            case TOTAL_PAY_BY_DIVISION -> buildTotalPayByDivisionReport(all);
        };
        reportOutput.setText(result);
        statusLabel.setText("Report generated: " + type.label);
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

    private String buildFullEmployeePayReport(List<Employee> employees) {
        StringBuilder sb = new StringBuilder();
        for (Employee e : employees) {
            sb.append("Employee #").append(e.getId() == null ? "?" : e.getId())
                    .append(" - ").append(defaultString(e.getName()))
                    .append(" | Job: ").append(e.getJobTitle() != null ? e.getJobTitle().getTitle() : "N/A")
                    .append(" | Division: ").append(e.getDivision() != null ? e.getDivision().getName() : "N/A")
                    .append(" | Salary: ").append(e.getSalary())
                    .append(" | Hire Date: ").append(e.getHireDate())
                    .append("\n");
            if (e.getPayroll() != null) {
                var p = e.getPayroll();
                sb.append("   Payroll ID ").append(p.getPayId())
                        .append(" | Pay Date: ").append(p.getPayDate())
                        .append(" | Earnings: ").append(p.getEarnings())
                        .append(" | State Tax: ").append(p.getStateTax())
                        .append(" | 401k: ").append(p.getRetire401k())
                        .append(" | Health: ").append(p.getHealthCare())
                        .append(" | Fed Tax: ").append(p.getFedTax())
                        .append(" | Fed Medical: ").append(p.getFedMedical())
                        .append(" | Fed SS: ").append(p.getFedSocialSecurity())
                        .append("\n");
            } else {
                sb.append("   No payroll data available\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private String buildTotalPayByJobReport(List<Employee> employees) {
        var totals = new java.util.LinkedHashMap<String, Double>();
        for (Employee e : employees) {
            String key = e.getJobTitle() != null ? e.getJobTitle().getTitle() : "Unassigned";
            totals.put(key, totals.getOrDefault(key, 0.0) + e.getPayForMonthByJob());
        }
        StringBuilder sb = new StringBuilder("Total pay for month by job title:\n");
        totals.forEach((job, total) -> sb.append(" - ").append(job).append(": ").append(total).append("\n"));
        return sb.toString();
    }

    private String buildTotalPayByDivisionReport(List<Employee> employees) {
        var totals = new java.util.LinkedHashMap<String, Double>();
        for (Employee e : employees) {
            String key = e.getDivision() != null ? e.getDivision().getName() : "Unassigned";
            totals.put(key, totals.getOrDefault(key, 0.0) + e.getPayForMonthByDivision());
        }
        StringBuilder sb = new StringBuilder("Total pay for month by division:\n");
        totals.forEach((division, total) -> sb.append(" - ").append(division).append(": ").append(total).append("\n"));
        return sb.toString();
    }

    private String defaultString(String value) {
        return value != null ? value : "";
    }

    private Double parseDouble(TextField field, String label) {
        if (field == null) {
            return null;
        }
        String text = field.getText() != null ? field.getText().trim() : "";
        if (text.isEmpty()) {
            showError(label, "Please enter " + label.toLowerCase() + ".");
            return null;
        }
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException ex) {
            showError(label, label + " must be a valid number.");
            return null;
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

    public enum ReportType {
        FULL_EMPLOYEE_PAY("Full-time employee information with pay statement history"),
        TOTAL_PAY_BY_JOB("Total pay for month by job title"),
        TOTAL_PAY_BY_DIVISION("Total pay for month by division");

        private final String label;

        ReportType(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
