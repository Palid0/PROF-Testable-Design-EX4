package es.upm.grise.profundizacion.td3;

import java.util.Vector;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProductDelivery {

	//clase para probar que la conexión falle
	public static class dummyConnectionClass {
		public Connection getConnection(String url) throws DatabaseProblemException {
            try {
                return DriverManager.getConnection(url);
            } catch (SQLException e) {
                throw new DatabaseProblemException();
            }
        }
	}
	
	Vector<Order> orders = new Vector<Order>();

	private dummyConnectionClass dummyConnection;
	
	public ProductDelivery(dummyConnectionClass dummyConnection) throws DatabaseProblemException {
		this.dummyConnection = dummyConnection;
		// Orders are loaded into the orders vector for processing
		try {
			
			// Create DB connection
			Connection connection = dummyConnection.getConnection("jdbc:sqlite:resources/orders.db");

			// Read from the orders table
			String query = "SELECT * FROM orders";
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query);

			// Iterate until we get all orders' data
			while (resultSet.next()) {
				
				int id = resultSet.getInt("id");
				double amount = resultSet.getDouble("amount");
				orders.add(new Order(id, amount));
				
			}

			// Close the connection
			connection.close();

		} catch (Exception e) {
			
			throw new DatabaseProblemException(); 
			
		}

	}

	// Calculate the handling amount
	public double calculateHandlingAmount() throws MissingOrdersException {
		// NODO 1

		// This method can only be invoked when there are orders to process
		if(orders.isEmpty()) // NODO 2
			throw new MissingOrdersException(); // HACIA NODO 8 (fin)
		
		// The handling amount is 2% of the orders' total amount
		double handlingPercentage = SystemConfiguration.getInstance().getHandlingPercentage();
		
		double totalAmount = 0;
		for(Order order : orders) { // NODO 3
			totalAmount += order.getAmount();	// NODO 4			
		}
		// NODO 5

		// However, it increases depending on the time of the day
		// We need to know the hour of the day. Minutes and seconds are not relevant
		SimpleDateFormat sdf = new SimpleDateFormat("HH");	
		Timestamp timestap = new Timestamp(System.currentTimeMillis());
		int hour = getHour(sdf, timestap);
			
		// and it also depends on the number of orders
		int numberOrders = getOrders(orders);
		
		// When it is late and the number of orders is large
		// the handling costs more
		if(hour >= 22 || numberOrders > 10) { // NODO 6a 1º condición, 6b 2º condición
			handlingPercentage += 0.01; // NODO 7
		}

		// The final handling amount
		return totalAmount * handlingPercentage; // HACIA NODO 8 (fin)
		
	}

	public int getOrders(Vector<Order> orders){
		return orders.size();
	}

	public int getHour(SimpleDateFormat sdf ,Timestamp timestap) {
        return Integer.valueOf(sdf.format(timestap));
    }
	
}