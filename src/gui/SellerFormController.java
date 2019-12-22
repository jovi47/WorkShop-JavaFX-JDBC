package gui;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import db.DbException;
import gui.listener.DataChangeListener;
import gui.util.Alerts;
import gui.util.Constraints;
import gui.util.Utils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import model.entities.Department;
import model.entities.Seller;
import model.exceptions.ValidateException;
import model.services.DepartmentService;
import model.services.SellerService;

public class SellerFormController implements Initializable {
	@FXML
	private TextField txtId;
	@FXML
	private TextField txtName;

	@FXML
	private TextField txtEmail;

	@FXML
	private DatePicker dpBirthDate;

	@FXML
	private TextField txtBaseSalary;

	@FXML
	private Label lblErrorName;

	@FXML
	private Label lblErrorEmail;

	@FXML
	private Label lblErrorBirthDate;

	@FXML
	private Label lblErrorBaseSalary;

	@FXML
	private Button btSave;

	@FXML
	private Button btCancel;

	@FXML
	private ComboBox<Department> comboBoxDepartment;

	private ObservableList<Department> obsList;

	private Seller entity;

	private SellerService service;

	private DepartmentService departmentService;

	private List<DataChangeListener> dataChangeListeners = new ArrayList<>();

	public void setServices(SellerService service, DepartmentService service1) {
		this.service = service;
		this.departmentService = service1;
	}

	public void setSeller(Seller entity) {
		this.entity = entity;
	}

	public void subscribeDataChangeListener(DataChangeListener listener) {
		dataChangeListeners.add(listener);
	}

	@Override
	public void initialize(URL url, ResourceBundle resource) {
		initializeNodes();
	}

	@FXML
	public void onBtSaveAction(ActionEvent event) {
		if (entity == null) {
			throw new IllegalStateException("Entity was null");
		}
		if (entity == null) {
			throw new IllegalStateException("Service was null");
		}
		try {
			entity = getFormData();
			service.saveOrUpdate(entity);
			notifyDataChangeListeners();
			Utils.currentStage(event).close();
		} catch (ValidateException e) {
			setErrorMessages(e.getErrors());
		} catch (DbException e) {
			Alerts.showAlert("Error saving object", null, e.getMessage(), AlertType.ERROR);
		}
	}

	private void notifyDataChangeListeners() {
		for (DataChangeListener listener : dataChangeListeners) {
			listener.onDataChanged();
		}
	}

	private Seller getFormData() {
		Seller sel = new Seller();
		ValidateException exception = new ValidateException("Validation error");
		sel.setId(Utils.tryParseToInt(txtId.getText()));
		if (txtName.getText() == null || txtName.getText().trim().equals("")) {
			exception.addError("name", "Field can't be empty");
		}
		sel.setName(txtName.getText());
		if (txtEmail.getText() == null || txtEmail.getText().trim().equals("")) {
			exception.addError("email", "Field can't be empty");
		}
		sel.setEmail(txtEmail.getText());
		if (dpBirthDate.getValue() == null) {
			exception.addError("birthDate", "Field can't be empty");
		} else {
			Instant instant = Instant.from(dpBirthDate.getValue().atStartOfDay(ZoneId.systemDefault()));
			Calendar x = Calendar.getInstance();
			x.setTime(Date.from(instant));
			sel.setBirthDate(x);
		}
		if (txtBaseSalary.getText() == null || txtBaseSalary.getText().trim().equals("")) {
			exception.addError("baseSalary", "Field can't be empty");
		}
		sel.setBaseSalary(Utils.tryParseToDouble(txtBaseSalary.getText()));

		sel.setDepartment(comboBoxDepartment.getValue());
		
		if (exception.getErrors().size() > 0) {
			throw exception;
		}
		return sel;
	}

	@FXML
	public void onBtCancelAction(ActionEvent event) {
		Utils.currentStage(event).close();
	}

	private void initializeNodes() {
		Constraints.setTextFieldInteger(txtId);
		Constraints.setTextFieldMaxLength(txtName, 50);
		Constraints.setTextFieldDouble(txtBaseSalary);
		Constraints.setTextFieldMaxLength(txtEmail, 50);
		Utils.formatDatePicker(dpBirthDate, "dd/MM/yyyy");
		initializeComboBoxDepartment();
	}

	public void updateFormData() {
		if (entity == null) {
			throw new IllegalStateException("Entity was null");
		}
		txtId.setText(String.valueOf(entity.getId()));
		txtName.setText(entity.getName());
		txtEmail.setText(entity.getEmail());
		Locale.setDefault(Locale.US);
		txtBaseSalary.setText(String.format("%.2f", entity.getBaseSalary()));
		Calendar x = entity.getBirthDate();
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		if (x != null) {
			String s = sdf.format(x.getTime());
			dpBirthDate.setValue(LOCAL_DATE(String.valueOf(s)));
		}
		if (entity.getDepartment() == null) {
			comboBoxDepartment.getSelectionModel().selectFirst();
		} else {
			comboBoxDepartment.setValue(entity.getDepartment());
		}
	}

	public void loadAssociatedObjects() {
		if (departmentService == null) {
			throw new IllegalStateException("Service was null");
		}
		List<Department> list = departmentService.findAll();
		obsList = FXCollections.observableArrayList(list);
		comboBoxDepartment.setItems(obsList);
	}

	private final LocalDate LOCAL_DATE(String dateString) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		LocalDate localDate = LocalDate.parse(dateString, formatter);
		return localDate;
	}

	private void setErrorMessages(Map<String, String> errors) {
		Set<String> fields = errors.keySet();
		lblErrorName.setText((fields.contains("name") ? errors.get("name") : ""));
		lblErrorEmail.setText((fields.contains("email") ? errors.get("email") : ""));
		lblErrorBaseSalary.setText((fields.contains("baseSalary") ? errors.get("baseSalary") : ""));
		lblErrorBirthDate.setText((fields.contains("birthDate") ? errors.get("birthDate") : ""));
	}

	private void initializeComboBoxDepartment() {
		Callback<ListView<Department>, ListCell<Department>> factory = lv -> new ListCell<Department>() {
			@Override
			protected void updateItem(Department item, boolean empty) {
				super.updateItem(item, empty);
				setText(empty ? "" : item.getName());

			}
		};

		comboBoxDepartment.setCellFactory(factory);
		comboBoxDepartment.setButtonCell(factory.call(null));
	}
}
