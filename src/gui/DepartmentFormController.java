package gui;

import java.net.URL;
import java.util.ResourceBundle;

import db.DbException;
import gui.util.Alerts;
import gui.util.Constraints;
import gui.util.Utils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import model.entities.Department;
import model.services.DepartmentService;

public class DepartmentFormController implements Initializable {
	@FXML
	private TextField txtId;
	@FXML
	private TextField txtName;
	@FXML
	private Label lblErrorName;
	@FXML
	private Button btSave;
	@FXML
	private Button btCancel;

	private Department entity;

	private DepartmentService service;

	public void setDepartmentService(DepartmentService service) {
		this.service = service;
	}

	public void setDepartment(Department entity) {
		this.entity = entity;
	}

	@Override
	public void initialize(URL url, ResourceBundle resource) {
		initializeNodes();
	}

	@FXML
	public void onBtSaveAction(ActionEvent event) {
		if(entity==null) {
			throw new IllegalStateException("Entity was null");
		}
		if(entity==null) {
			throw new IllegalStateException("Service was null");
		}
		try {
			entity = getFormData();
			service.saveOrUpdate(entity);
			Utils.currentStage(event).close();
		} catch (DbException e) {
			Alerts.showAlert("Error saving object", null, e.getMessage(), AlertType.ERROR);
		}
	}

	private Department getFormData() {
		return new Department(Utils.tryParseToInt(txtId.getText()), txtName.getText());
	}

	@FXML
	public void onBtCancelAction(ActionEvent event) {
	Utils.currentStage(event).close();
	}

	private void initializeNodes() {
		Constraints.setTextFieldInteger(txtId);
		Constraints.setTextFieldMaxLength(txtName, 30);
	}

	public void updateFormData() {
		if (entity == null) {
			throw new IllegalStateException("Entity was null");
		}
		txtId.setText(String.valueOf(entity.getId()));
		txtName.setText(entity.getName());
	}
}
