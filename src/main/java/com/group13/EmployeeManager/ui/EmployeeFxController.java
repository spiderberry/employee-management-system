package com.group13.EmployeeManager.ui;

import com.group13.EmployeeManager.entity.Employee;
import com.group13.EmployeeManager.entity.Division;
import com.group13.EmployeeManager.entity.Job;
import com.group13.EmployeeManager.entity.Payroll;
import com.group13.EmployeeManager.ui.client.BackendClient;
import com.group13.EmployeeManager.ui.viewmodel.EmployeeReportBuilder;
import com.group13.EmployeeManager.ui.viewmodel.PayrollFormData;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;

@Component
public class EmployeeFxController {

    @Autowired
    private com.group13.EmployeeManager.controller.EmployeeController employeeController;
    @Autowired
    private BackendClient backendClient;
    private final EmployeeReportBuilder reportBuilder = new EmployeeReportBuilder();
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
    private ComboBox<String> jobField;
    @FXML
    private ComboBox<String> divisionField;
    @FXML
    private TextField minSalaryRangeField;
    @FXML
    private TextField maxSalaryRangeField;
    @FXML
    private TextField adjustmentValueField;
    @FXML
    private ComboBox<AdjustMode> adjustmentModeBox;
    @FXML
    private DatePicker payDatePicker;
    @FXML
    private TextField earningsField;
    @FXML
    private TextField stateTaxField;
    @FXML
    private TextField retire401kField;
    @FXML
    private TextField healthCareField;
    @FXML
    private TextField fedTaxField;
    @FXML
    private TextField fedMedicalField;
    @FXML
    private TextField fedSocialField;
    @FXML
    private DatePicker hireDatePicker;
    @FXML
    private Label statusLabel;
    @FXML
    private ComboBox<ReportType> reportSelector;
    @FXML
    private TextArea reportOutput;

    @FXML
    public void initialize() {
        searchModeBox.getItems().setAll(SearchMode.values());
        searchModeBox.getSelectionModel().select(SearchMode.NAME);
        if (reportSelector != null) {
            reportSelector.getItems().setAll(ReportType.values());
            reportSelector.getSelectionModel().select(ReportType.FULL_EMPLOYEE_PAY);
        }
        if (adjustmentModeBox != null) {
            adjustmentModeBox.getItems().setAll(AdjustMode.values());
            adjustmentModeBox.getSelectionModel().select(AdjustMode.PERCENT);
        }
        if (jobField != null) {
            jobField.setEditable(true);
            jobField.getItems().setAll(fetchJobTitles());
        }
        if (divisionField != null) {
            divisionField.setEditable(true);
            divisionField.getItems().setAll(fetchDivisionNames());
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
                        results.add(employeeController.getEmployeeById(id));
                    }
                }
                case NAME -> {
                    results.addAll(findEmployeesByName(term));
                }
                case SSN -> {
                    Employee employee = findEmployeeBySsn(term);
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
            Employee saved = employeeController.addEmployee(employee);
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
            Employee saved = employeeController.updateEmployee(current);
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
            employeeController.deleteEmployee(id);
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
    private void handleSavePayroll() {
        Employee employee = resolveEmployeeForUpdate();
        if (employee == null) {
            showError("Payroll", "Select an employee before saving payroll.");
            return;
        }

        LocalDate payDate = payDatePicker.getValue();
        if (payDate == null) {
            showError("Payroll", "Pay date is required.");
            return;
        }

        Double earnings = parseDouble(earningsField, "Earnings");
        Double stateTax = parseDouble(stateTaxField, "State tax");
        Double retire401k = parseDouble(retire401kField, "401k");
        Double healthCare = parseDouble(healthCareField, "Health care");
        Double fedTax = parseDouble(fedTaxField, "Federal tax");
        Double fedMedical = parseDouble(fedMedicalField, "Federal medical");
        Double fedSocial = parseDouble(fedSocialField, "Federal social security");

        if (earnings == null || stateTax == null || retire401k == null || healthCare == null || fedTax == null || fedMedical == null || fedSocial == null) {
            return;
        }

        PayrollFormData payrollFormData = new PayrollFormData();
        payrollFormData.payDate = payDate;
        payrollFormData.earnings = earnings;
        payrollFormData.stateTax = stateTax;
        payrollFormData.retire401k = retire401k;
        payrollFormData.healthCare = healthCare;
        payrollFormData.fedTax = fedTax;
        payrollFormData.fedMedical = fedMedical;
        payrollFormData.fedSocial = fedSocial;

        savePayrollForEmployee(employee, payrollFormData);
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
        Double value = parseDouble(adjustmentValueField, "Adjustment value");
        AdjustMode mode = Optional.ofNullable(adjustmentModeBox != null ? adjustmentModeBox.getValue() : AdjustMode.PERCENT)
                .orElse(AdjustMode.PERCENT);

        if (min == null || max == null || value == null) {
            return;
        }
        if (min < 0 || max <= 0 || max <= min) {
            showError("Salary range", "Enter a valid salary range where max is greater than min.");
            return;
        }

        List<Employee> all = employeeController.getAllEmployees();
        int updated = 0;
        for (Employee e : all) {
            double salary = e.getSalary();
            if (salary >= min && salary < max) {
                double delta = mode == AdjustMode.PERCENT ? salary * (value / 100.0) : value;
                double updatedSalary = salary + delta;
                e.setSalary(updatedSalary);
                employeeController.updateEmployee(e);
                updated++;
            }
        }
        refreshTable();
        String label = mode == AdjustMode.PERCENT ? value + "% change" : "amount change of " + value;
        statusLabel.setText("Applied " + label + " to " + updated + " employee(s) in range.");
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
        List<Employee> all = employeeController.getAllEmployees();
        String result = switch (type) {
            case FULL_EMPLOYEE_PAY -> reportBuilder.fullEmployeePay(all);
            case TOTAL_PAY_BY_JOB -> reportBuilder.totalPayByJob(all);
            case TOTAL_PAY_BY_DIVISION -> reportBuilder.totalPayByDivision(all);
        };
        reportOutput.setText(result);
        statusLabel.setText("Report generated: " + type.label);
    }

    private void refreshTable() {
        employees.setAll(employeeController.getAllEmployees());
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
        jobField.getEditor().setText(employee.getJobTitle() != null ? employee.getJobTitle().getTitle() : "");
        divisionField.getEditor().setText(employee.getDivision() != null ? employee.getDivision().getName() : "");
        populatePayrollForm(employee.getPayroll());
    }

    private void savePayrollForEmployee(Employee employee, PayrollFormData payrollFormData) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("payDate", payrollFormData.payDate);
        payload.put("earnings", payrollFormData.earnings);
        payload.put("stateTax", payrollFormData.stateTax);
        payload.put("retire401k", payrollFormData.retire401k);
        payload.put("healthCare", payrollFormData.healthCare);
        Map<String, Object> fedInfo = new HashMap<>();
        fedInfo.put("tax", payrollFormData.fedTax);
        fedInfo.put("medical", payrollFormData.fedMedical);
        fedInfo.put("socialSecurtiy", payrollFormData.fedSocial);
        payload.put("fedInfo", fedInfo);
        Map<String, Object> employeeRef = new HashMap<>();
        employeeRef.put("id", employee.getId());
        payload.put("employee", employeeRef);

        Map<String, Object> response = backendClient.postPayroll(payload);
        Long payId = null;
        if (response != null && response.get("payId") instanceof Number num) {
            payId = num.longValue();
        }

        if (payId != null) {
            Payroll saved = new Payroll();
            saved.setPayId(payId);
            saved.setPayDate(payrollFormData.payDate);
            saved.setEarnings(payrollFormData.earnings);
            saved.setStateTax(payrollFormData.stateTax);
            saved.setRetire401k(payrollFormData.retire401k);
            saved.setHealthCare(payrollFormData.healthCare);
            setFieldValue(saved, "fedTax", payrollFormData.fedTax);
            setFieldValue(saved, "fedMedical", payrollFormData.fedMedical);
            setFieldValue(saved, "fedSocialSecurity", payrollFormData.fedSocial);

            employee.setPayroll(saved);
            employeeController.updateEmployee(employee);
            populatePayrollForm(saved);
            statusLabel.setText("Saved payroll for employee #" + employee.getId());
        } else {
            showError("Payroll", "Saved payroll but could not read returned payId.");
        }
    }

    private void clearForm() {
        idField.clear();
        nameField.clear();
        emailField.clear();
        ssnField.clear();
        salaryField.clear();
        hireDatePicker.setValue(null);
        jobField.getEditor().clear();
        divisionField.getEditor().clear();
        payDatePicker.setValue(null);
        earningsField.clear();
        stateTaxField.clear();
        retire401kField.clear();
        healthCareField.clear();
        fedTaxField.clear();
        fedMedicalField.clear();
        fedSocialField.clear();
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
        String jobValue = jobField.getEditor().getText();
        formData.jobTitle = jobValue != null && !jobValue.trim().isEmpty()
                ? jobValue.trim()
                : null;
        String divisionValue = divisionField.getEditor().getText();
        formData.divisionName = divisionValue != null && !divisionValue.trim().isEmpty()
                ? divisionValue.trim()
                : null;
        formData.hireDate = employee.getHireDate();
        return formData;
    }

    private Employee resolveEmployeeForUpdate() {
        Employee selected = employeeTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            return employeeController.getEmployeeById(selected.getId());
        }

        Long id = parseId(idField.getText());
        if (id == null) {
            showError("Update employee", "Select a row or enter a valid employee ID to update.");
            return null;
        }

        try {
            return employeeController.getEmployeeById(id);
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
        boolean updated = false;

        if (formData.jobTitle != null) {
            Job job = ensureJob(formData.jobTitle);
            if (job != null) {
                employee.setJobTitle(job);
                updated = true;
            }
        }
        if (formData.divisionName != null) {
            Division division = ensureDivision(formData.divisionName);
            if (division != null) {
                employee.setDivision(division);
                updated = true;
            }
        }

        if (updated) {
            employeeController.updateEmployee(employee);
        }
    }

    private void populatePayrollForm(Payroll payroll) {
        if (payroll == null) {
            payDatePicker.setValue(null);
            earningsField.clear();
            stateTaxField.clear();
            retire401kField.clear();
            healthCareField.clear();
            fedTaxField.clear();
            fedMedicalField.clear();
            fedSocialField.clear();
            return;
        }
        payDatePicker.setValue(payroll.getPayDate());
        earningsField.setText(Double.toString(payroll.getEarnings()));
        stateTaxField.setText(Double.toString(payroll.getStateTax()));
        retire401kField.setText(Double.toString(payroll.getRetire401k()));
        healthCareField.setText(Double.toString(payroll.getHealthCare()));
        fedTaxField.setText(Double.toString(payroll.getFedTax()));
        fedMedicalField.setText(Double.toString(payroll.getFedMedical()));
        fedSocialField.setText(Double.toString(payroll.getFedSocialSecurity()));
    }

    private void setFieldValue(Object target, String fieldName, Object value) {
        if (target == null || value == null) {
            return;
        }
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
        }
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

    private List<Employee> findEmployeesByName(String name) {
        String normalized = name.toLowerCase();
        return employeeController.getAllEmployees().stream()
                .filter(emp -> emp.getName() != null && emp.getName().toLowerCase().contains(normalized))
                .toList();
    }

    private Employee findEmployeeBySsn(String ssn) {
        return employeeController.getAllEmployees().stream()
                .filter(emp -> ssn.equals(emp.getSocialSecurityNumber()))
                .findFirst()
                .orElse(null);
    }

    private Job ensureJob(String title) {
        Job existing = backendClient.fetchJobByTitle(title);
        if (existing != null) {
            return existing;
        }
        Job created = backendClient.createJob(new Job(null, title));
        if (created == null) {
            showError("Job", "Unable to save job \"" + title + "\"");
        }
        return created;
    }

    private Division ensureDivision(String name) {
        Division existing = backendClient.fetchDivisionByName(name);
        if (existing != null) {
            return existing;
        }
        Division request = new Division();
        request.setName(name);
        Division created = backendClient.createDivision(request);
        if (created == null) {
            showError("Division", "Unable to save division \"" + name + "\"");
        }
        return created;
    }

    private List<String> fetchJobTitles() {
        return backendClient.fetchJobs().stream().map(Job::getTitle).toList();
    }

    private List<String> fetchDivisionNames() {
        return backendClient.fetchDivisions().stream().map(Division::getName).toList();
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

    public enum AdjustMode {
        PERCENT("Percent"),
        AMOUNT("Flat Amount");

        private final String label;

        AdjustMode(String label) {
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
