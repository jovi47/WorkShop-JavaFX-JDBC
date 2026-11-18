import db.DB;
import gui.SellerFormController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import model.entities.Department;
import model.entities.Seller;
import model.services.SellerService;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.nio.file.Paths;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

public class SellerFormValidationTest {
	public static void main(String[] args) throws Exception {
		FutureTask<Void> test = new FutureTask<>(() -> {
			String scenario = args.length == 0 ? "all" : args[0];
			if (scenario.equals("salary") || scenario.equals("all")) {
				rejects("", true, "lblErrorBaseSalary", "Enter a valid salary");
				rejects(".", true, "lblErrorBaseSalary", "Enter a valid salary");
				rejects(new String(new char[400]).replace('\0', '9'), true,
						"lblErrorBaseSalary", "Enter a valid salary");
			}
			if (scenario.equals("department") || scenario.equals("all")) {
				rejects("100.50", false, "lblErrorDepartment", "Choose a department");
			}
			return null;
		});
		Platform.startup(test);
		try {
			test.get(20, TimeUnit.SECONDS);
		} finally {
			Platform.exit();
		}
	}

	@SuppressWarnings("unchecked")
	private static void rejects(String salary, boolean selectDepartment, String errorId,
			String message) throws Exception {
		// Wire the real service to a connection that detects any attempted database write.
		Field connection = DB.class.getDeclaredField("conn");
		connection.setAccessible(true);
		connection.set(null, Proxy.newProxyInstance(Connection.class.getClassLoader(),
				new Class<?>[] {Connection.class}, (proxy, method, args) -> {
					throw new AssertionError("Invalid form reached JDBC: " + method.getName());
				}));
		try {
			FXMLLoader loader = new FXMLLoader(Paths.get("src/gui/SellerForm.fxml").toUri().toURL());
			Parent root = loader.load();
			new Scene(root);
			SellerFormController controller = loader.getController();
			controller.setSeller(new Seller());
			controller.setServices(new SellerService(), null);
			Map<String, Object> fields = loader.getNamespace();
			((TextField) fields.get("txtName")).setText("Synthetic Seller");
			((TextField) fields.get("txtEmail")).setText("seller@example.test");
			((DatePicker) fields.get("dpBirthDate")).setValue(LocalDate.of(1990, 1, 15));
			((TextField) fields.get("txtBaseSalary")).setText(salary);
			if (selectDepartment) {
				((ComboBox<Department>) fields.get("comboBoxDepartment")).setValue(new Department(1, "Sales"));
			}
			((Button) fields.get("btSave")).fire();
			Label error = (Label) fields.get(errorId);
			if (error == null || !message.equals(error.getText())) {
				throw new AssertionError("Expected field error: " + message);
			}
		} finally {
			connection.set(null, null);
		}
	}

}
