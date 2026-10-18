package com.hrms;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

    private static final String URL = "jdbc:mysql://localhost:3306/hrms_db";
    private static final String USER = "root";
    private static final String PASS = "Zxcvbnm@1234";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return DriverManager.getConnection(URL, USER, PASS);
    }

    public static void initDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS organizations (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT, " +
                    "name VARCHAR(150) NOT NULL, " +
                    "address VARCHAR(255) NOT NULL, " +
                    "pan VARCHAR(30) NOT NULL, " +
                    "gst VARCHAR(30) NOT NULL, " +
                    "department VARCHAR(100) NOT NULL, " +
                    "email VARCHAR(150) NOT NULL UNIQUE, " +
                    "password VARCHAR(100) NOT NULL, " +
                    "employees_count INT NOT NULL DEFAULT 0, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS auth_tokens (" +
                    "token_id INT PRIMARY KEY AUTO_INCREMENT, " +
                    "org_id INT NOT NULL, " +
                    "token_value VARCHAR(100) NOT NULL, " +
                    "user_email VARCHAR(150) NOT NULL, " +
                    "role VARCHAR(50) NOT NULL DEFAULT 'Admin', " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (org_id) REFERENCES organizations(id) ON DELETE CASCADE)");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS employees (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT, " +
                    "org_id INT NOT NULL, " +
                    "name VARCHAR(100) NOT NULL, " +
                    "email VARCHAR(150) NOT NULL, " +
                    "phone VARCHAR(30) NOT NULL, " +
                    "role VARCHAR(50) NOT NULL DEFAULT 'Staff', " +
                    "department VARCHAR(100) NOT NULL, " +
                    "salary DOUBLE NOT NULL DEFAULT 0.0, " +
                    "join_date VARCHAR(30) NOT NULL, " +
                    "FOREIGN KEY (org_id) REFERENCES organizations(id) ON DELETE CASCADE)");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS attendance (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT, " +
                    "org_id INT NOT NULL, " +
                    "employee_id INT NOT NULL, " +
                    "date VARCHAR(30) NOT NULL, " +
                    "status VARCHAR(30) NOT NULL, " +
                    "remarks VARCHAR(255), " +
                    "FOREIGN KEY (org_id) REFERENCES organizations(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE)");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS salaries (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT, " +
                    "org_id INT NOT NULL, " +
                    "employee_id INT NOT NULL, " +
                    "month_year VARCHAR(30) NOT NULL, " +
                    "basic_salary DOUBLE NOT NULL, " +
                    "bonus DOUBLE NOT NULL DEFAULT 0.0, " +
                    "deductions DOUBLE NOT NULL DEFAULT 0.0, " +
                    "net_salary DOUBLE NOT NULL, " +
                    "status VARCHAR(30) NOT NULL DEFAULT 'Pending', " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (org_id) REFERENCES organizations(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE)");

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS notices_holidays (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT, " +
                    "org_id INT NOT NULL, " +
                    "type VARCHAR(30) NOT NULL, " +
                    "title VARCHAR(200) NOT NULL, " +
                    "description TEXT NOT NULL, " +
                    "event_date VARCHAR(50) NOT NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (org_id) REFERENCES organizations(id) ON DELETE CASCADE)");
        } catch (SQLException e) {
            System.out.println("Database init error: " + e.getMessage());
        }
    }

    public static boolean emailExists(String email) {
        String sql = "SELECT id FROM organizations WHERE email = ?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }

    public static boolean insertOrganization(String name, String address, String pan, String gst,
                                             String department, String email, String password, int employeesCount) {
        String sql = "INSERT INTO organizations (name, address, pan, gst, department, email, password, employees_count) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, address);
            stmt.setString(3, pan);
            stmt.setString(4, gst);
            stmt.setString(5, department);
            stmt.setString(6, email);
            stmt.setString(7, password);
            stmt.setInt(8, employeesCount);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }

    public static String[] checkLogin(String email, String password) {
        String sql = "SELECT id, name FROM organizations WHERE email = ? AND password = ?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, password);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new String[]{String.valueOf(rs.getInt("id")), rs.getString("name")};
                }
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return null;
    }

    public static void saveToken(int orgId, String token, String email, String role) {
        String sql = "INSERT INTO auth_tokens (org_id, token_value, user_email, role) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orgId);
            stmt.setString(2, token);
            stmt.setString(3, email);
            stmt.setString(4, role);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static boolean updatePassword(String email, String newPassword) {
        String sql = "UPDATE organizations SET password = ? WHERE email = ?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newPassword);
            stmt.setString(2, email);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }

    public static boolean insertEmployee(int orgId, String name, String email, String phone,
                                         String role, String dept, double salary, String date) {
        String sql = "INSERT INTO employees (org_id, name, email, phone, role, department, salary, join_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orgId);
            stmt.setString(2, name);
            stmt.setString(3, email);
            stmt.setString(4, phone);
            stmt.setString(5, role);
            stmt.setString(6, dept);
            stmt.setDouble(7, salary);
            stmt.setString(8, date);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }

    public static int getEmployees(int orgId, int[] ids, String[] names, String[] roles,
                                   String[] depts, String[] emails, double[] salaries, String[] dates) {
        int count = 0;
        String sql = "SELECT * FROM employees WHERE org_id = ?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orgId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next() && count < ids.length) {
                    ids[count] = rs.getInt("id");
                    names[count] = rs.getString("name");
                    roles[count] = rs.getString("role");
                    depts[count] = rs.getString("department");
                    emails[count] = rs.getString("email");
                    salaries[count] = rs.getDouble("salary");
                    dates[count] = rs.getString("join_date");
                    count++;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return count;
    }

    public static boolean updateEmployee(int orgId, int id, String name, String email,
                                         String phone, String dept, double salary) {
        String sql = "UPDATE employees SET name=?, email=?, phone=?, department=?, salary=? WHERE id=? AND org_id=?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, phone);
            stmt.setString(4, dept);
            stmt.setDouble(5, salary);
            stmt.setInt(6, id);
            stmt.setInt(7, orgId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }

    public static boolean deleteEmployee(int orgId, int id) {
        String sql = "DELETE FROM employees WHERE id=? AND org_id=?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.setInt(2, orgId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }

    public static boolean insertAttendance(int orgId, int empId, String date, String status, String remarks) {
        String sql = "INSERT INTO attendance (org_id, employee_id, date, status, remarks) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orgId);
            stmt.setInt(2, empId);
            stmt.setString(3, date);
            stmt.setString(4, status);
            stmt.setString(5, remarks);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }

    public static int getAttendance(int orgId, int[] ids, int[] empIds, String[] dates,
                                    String[] statuses, String[] remarks) {
        int count = 0;
        String sql = "SELECT * FROM attendance WHERE org_id = ?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orgId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next() && count < ids.length) {
                    ids[count] = rs.getInt("id");
                    empIds[count] = rs.getInt("employee_id");
                    dates[count] = rs.getString("date");
                    statuses[count] = rs.getString("status");
                    remarks[count] = rs.getString("remarks");
                    count++;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return count;
    }

    public static boolean updateAttendance(int orgId, int id, String status, String remarks) {
        String sql = "UPDATE attendance SET status=?, remarks=? WHERE id=? AND org_id=?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setString(2, remarks);
            stmt.setInt(3, id);
            stmt.setInt(4, orgId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }

    public static boolean deleteAttendance(int orgId, int id) {
        String sql = "DELETE FROM attendance WHERE id=? AND org_id=?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.setInt(2, orgId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }

    public static boolean insertSalary(int orgId, int empId, String monthYear, double basic,
                                       double bonus, double deductions, double netSalary, String status) {
        String sql = "INSERT INTO salaries (org_id, employee_id, month_year, basic_salary, bonus, deductions, net_salary, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orgId);
            stmt.setInt(2, empId);
            stmt.setString(3, monthYear);
            stmt.setDouble(4, basic);
            stmt.setDouble(5, bonus);
            stmt.setDouble(6, deductions);
            stmt.setDouble(7, netSalary);
            stmt.setString(8, status);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }

    public static int getSalaries(int orgId, int[] ids, int[] empIds, String[] months,
                                  double[] basics, double[] bonuses, double[] deducts,
                                  double[] nets, String[] statuses) {
        int count = 0;
        String sql = "SELECT * FROM salaries WHERE org_id = ?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orgId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next() && count < ids.length) {
                    ids[count] = rs.getInt("id");
                    empIds[count] = rs.getInt("employee_id");
                    months[count] = rs.getString("month_year");
                    basics[count] = rs.getDouble("basic_salary");
                    bonuses[count] = rs.getDouble("bonus");
                    deducts[count] = rs.getDouble("deductions");
                    nets[count] = rs.getDouble("net_salary");
                    statuses[count] = rs.getString("status");
                    count++;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return count;
    }

    public static boolean updateSalary(int orgId, int id, double basic, double bonus,
                                       double deductions, double netSalary, String status) {
        String sql = "UPDATE salaries SET basic_salary=?, bonus=?, deductions=?, net_salary=?, status=? WHERE id=? AND org_id=?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, basic);
            stmt.setDouble(2, bonus);
            stmt.setDouble(3, deductions);
            stmt.setDouble(4, netSalary);
            stmt.setString(5, status);
            stmt.setInt(6, id);
            stmt.setInt(7, orgId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }

    public static boolean deleteSalary(int orgId, int id) {
        String sql = "DELETE FROM salaries WHERE id=? AND org_id=?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.setInt(2, orgId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }

    public static boolean insertNoticeHoliday(int orgId, String type, String title, String desc, String date) {
        String sql = "INSERT INTO notices_holidays (org_id, type, title, description, event_date) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orgId);
            stmt.setString(2, type);
            stmt.setString(3, title);
            stmt.setString(4, desc);
            stmt.setString(5, date);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }

    public static int getNoticesHolidays(int orgId, int[] ids, String[] types,
                                         String[] titles, String[] descs, String[] dates) {
        int count = 0;
        String sql = "SELECT * FROM notices_holidays WHERE org_id = ?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orgId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next() && count < ids.length) {
                    ids[count] = rs.getInt("id");
                    types[count] = rs.getString("type");
                    titles[count] = rs.getString("title");
                    descs[count] = rs.getString("description");
                    dates[count] = rs.getString("event_date");
                    count++;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return count;
    }

    public static boolean updateNoticeHoliday(int orgId, int id, String type, String title, String desc, String date) {
        String sql = "UPDATE notices_holidays SET type=?, title=?, description=?, event_date=? WHERE id=? AND org_id=?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, type);
            stmt.setString(2, title);
            stmt.setString(3, desc);
            stmt.setString(4, date);
            stmt.setInt(5, id);
            stmt.setInt(6, orgId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }

    public static boolean deleteNoticeHoliday(int orgId, int id) {
        String sql = "DELETE FROM notices_holidays WHERE id=? AND org_id=?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.setInt(2, orgId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }
}
