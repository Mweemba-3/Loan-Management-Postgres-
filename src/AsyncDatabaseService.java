import javax.swing.*;
import java.sql.*;
import java.util.concurrent.*;
import java.util.function.*;

public class AsyncDatabaseService {
    
    // Execute query asynchronously
    public static <T> void executeAsync(Callable<T> databaseTask, 
                                       Consumer<T> onSuccess, 
                                       Consumer<Exception> onError) {
        
        SwingWorker<T, Void> worker = new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return databaseTask.call();
            }
            
            @Override
            protected void done() {
                try {
                    T result = get();
                    if (onSuccess != null) {
                        onSuccess.accept(result);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    if (onError != null) {
                        onError.accept(e);
                    }
                } catch (ExecutionException e) {
                    if (onError != null) {
                        onError.accept(e.getCause() instanceof Exception ? 
                                     (Exception) e.getCause() : new Exception(e.getCause()));
                    }
                } catch (CancellationException e) {
                    System.out.println("Database task was cancelled");
                }
            }
        };
        
        worker.execute();
    }
    
    // Quick async log (for audit logs)
    public static void logAsync(int employeeId, String action, String details) {
        executeAsync(() -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "INSERT INTO audit_logs (employee_id, employee_name, action, details) " +
                           "SELECT ?, name, ?, ? FROM employees WHERE employee_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, employeeId);
                    stmt.setString(2, action);
                    stmt.setString(3, details);
                    stmt.setInt(4, employeeId);
                    DatabaseConnection.executeUpdateWithTimeout(stmt, 5);
                }
            }
            return null;
        }, null, e -> System.err.println("Async log failed: " + e.getMessage()));
    }
}