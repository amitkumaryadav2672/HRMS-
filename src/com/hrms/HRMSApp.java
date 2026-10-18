package com.hrms;

import java.util.Scanner;

public class HRMSApp {

    public static void main(String[] args) {
        Database.initDatabase();
        Scanner sc = new Scanner(System.in);


        while (true) {
            System.out.println("HRMS project");
            System.out.println("TASK -1\n");
            System.out.println("HRMS project");
            System.out.println("1 first terminal\n");
            System.out.println("select 3 options");
            System.out.println("1 . Create new Organization");
            System.out.println("2. Login to Organization");
            System.out.println("3. Exit");

            int choice = readInt(sc, "Enter choice: ");

            if (choice == 1) {
                createOrganization(sc);
            } else if (choice == 2) {
                loginMenu(sc);
            } else if (choice == 3) {
                System.out.println("Exit");
                break;
            } else {
                System.out.println("Invalid option. Try again.");
            }
        }
        sc.close();
    }

    private static void createOrganization(Scanner sc) {
        String name = readLine(sc, "Organization name: ");
        String address = readLine(sc, "Address: ");
        String pan = readLine(sc, "pan: ");
        String gst = readLine(sc, "gst: ");
        String department = readLine(sc, "Department: ");
        String email = readLine(sc, "email (for login access): ");
        String password = readLine(sc, "password: ");
        int employeesCount = readInt(sc, "organization employees count: ");

        if (Database.emailExists(email)) {
            System.out.println("Email already registered.");
            return;
        }

        boolean success = Database.insertOrganization(name, address, pan, gst, department, email, password, employeesCount);
        if (success) {
            System.out.println("\nif Success\n{\n1 . Create new Organization\n2. Login to Organization\n3. Exit\n}");
        }
    }

    private static void loginMenu(Scanner sc) {
        while (true) {
            System.out.println("\nSelect the options");
            System.out.println("1. Enter email and pass word for login");
            System.out.println("2. forget password");
            System.out.println("3. back to main menu");
            System.out.println("4. exit");

            int opt = readInt(sc, "Enter choice: ");

            if (opt == 1) {
                if (loginUser(sc)) {
                    break;
                }
            } else if (opt == 2) {
                forgetPassword(sc);
            } else if (opt == 3) {
                break;
            } else if (opt == 4) {
                System.out.println("Exiting program.");
                System.exit(0);
            } else {
                System.out.println("Invalid option.");
            }
        }
    }

    private static boolean loginUser(Scanner sc) {
        System.out.println("\nif 1 {");
        String email = readLine(sc, "Enter email -> ");
        String password = readLine(sc, "enter password -> ");

        String[] data = Database.checkLogin(email, password);
        if (data != null) {
            int orgId = Integer.parseInt(data[0]);
            String orgName = data[1];
            String token = "TOKEN-" + (System.currentTimeMillis() % 1000000);
            String role = "Admin";

            Database.saveToken(orgId, token, email, role);

            System.out.println("\nif Success{");
            System.out.println("  Token: " + token);
            System.out.println("  Role: " + role);
            System.out.println("  WElcome to " + orgName);
            System.out.println("}");

            dashboard(sc, orgId, orgName);
            return true;
        } else {
            System.out.println("Invalid email or password.");
            return false;
        }
    }

    private static void forgetPassword(Scanner sc) {
        System.out.println("\nif 2 {");
        System.out.println("Forget password");
        String email = readLine(sc, "Enter email-> ");
        String newPassword = readLine(sc, "enter new password for login -> ");

        boolean success = Database.updatePassword(email, newPassword);
        if (success) {
            System.out.println("\nif success {");
            System.out.println("1. Enter email and pass word for login");
            System.out.println("2. forget password");
            System.out.println("3. back to main menu");
            System.out.println("4. exit");
            System.out.println("}");
        } else {
            System.out.println("Email not found.");
        }
        System.out.println("}");
    }

    private static void dashboard(Scanner sc, int orgId, String orgName) {
        while (true) {
            System.out.println("\nWElcome to " + orgName);
            System.out.println("select the options");
            System.out.println("1 employee -> add,view , udpate ,delete");
            System.out.println("2 attendance -> create , view ,udpate ,delete");
            System.out.println("3 salary -> for particular employee -> create salary , udpate ,delete ,view");
            System.out.println("4. notice or holiday -> create , view , delete , update for all employee");
            System.out.println("5 logout -> redirect to main menu");

            int opt = readInt(sc, "Enter choice: ");

            if (opt == 1) {
                employeeSection(sc, orgId);
            } else if (opt == 2) {
                attendanceSection(sc, orgId);
            } else if (opt == 3) {
                salarySection(sc, orgId);
            } else if (opt == 4) {
                noticeHolidaySection(sc, orgId);
            } else if (opt == 5) {
                System.out.println("5 logout -> redirect to main menu\n");
                break;
            } else {
                System.out.println("Invalid option.");
            }
        }
    }

    private static void employeeSection(Scanner sc, int orgId) {
        while (true) {
            System.out.println("\n--- Employee Options ---");
            System.out.println("1. Add Employee");
            System.out.println("2. View Employees");
            System.out.println("3. Update Employee");
            System.out.println("4. Delete Employee");
            System.out.println("5. Back");

            int opt = readInt(sc, "Choice: ");

            if (opt == 1) {
                String name = readLine(sc, "Name: ");
                String email = readLine(sc, "Email: ");
                String phone = readLine(sc, "Phone: ");
                System.out.println("Role: 1. Superadmin | 2. Admin | 3. Staff | 4. Users");
                int r = readInt(sc, "Choice: ");
                String role = (r == 1) ? "Superadmin" : (r == 2 ? "Admin" : (r == 3 ? "Staff" : "Users"));
                String dept = readLine(sc, "Department: ");
                double salary = readDouble(sc, "Salary: ");
                String date = readLine(sc, "Join Date (YYYY-MM-DD): ");

                boolean success = Database.insertEmployee(orgId, name, email, phone, role, dept, salary, date);
                if (success) {
                    System.out.println("Employee added successfully.");
                }
            } else if (opt == 2) {
                viewEmployees(orgId);
            } else if (opt == 3) {
                viewEmployees(orgId);
                int id = readInt(sc, "Enter Employee ID to update: ");
                String name = readLine(sc, "New Name: ");
                String email = readLine(sc, "New Email: ");
                String phone = readLine(sc, "New Phone: ");
                String dept = readLine(sc, "New Department: ");
                double salary = readDouble(sc, "New Salary: ");

                boolean success = Database.updateEmployee(orgId, id, name, email, phone, dept, salary);
                if (success) {
                    System.out.println("Employee updated.");
                } else {
                    System.out.println("Employee not found.");
                }
            } else if (opt == 4) {
                viewEmployees(orgId);
                int id = readInt(sc, "Enter Employee ID to delete: ");
                boolean success = Database.deleteEmployee(orgId, id);
                if (success) {
                    System.out.println("Employee deleted.");
                } else {
                    System.out.println("Employee not found.");
                }
            } else if (opt == 5) {
                break;
            } else {
                System.out.println("Invalid option.");
            }
        }
    }

    private static void viewEmployees(int orgId) {
        int max = 200;
        int[] ids = new int[max];
        String[] names = new String[max];
        String[] roles = new String[max];
        String[] depts = new String[max];
        String[] emails = new String[max];
        double[] salaries = new double[max];
        String[] dates = new String[max];

        int count = Database.getEmployees(orgId, ids, names, roles, depts, emails, salaries, dates);

        System.out.println("\nID | Name | Role | Department | Email | Salary | Date");
        System.out.println("---------------------------------------------------------------");
        if (count == 0) {
            System.out.println("No employees found.");
        } else {
            for (int i = 0; i < count; i++) {
                System.out.println(ids[i] + " | " + names[i] + " | " + roles[i] + " | " + depts[i] + " | " + emails[i] + " | " + salaries[i] + " | " + dates[i]);
            }
        }
        System.out.println();
    }

    private static void attendanceSection(Scanner sc, int orgId) {
        while (true) {
            System.out.println("\n--- Attendance Options ---");
            System.out.println("1. Create Attendance");
            System.out.println("2. View Attendance");
            System.out.println("3. Update Attendance");
            System.out.println("4. Delete Attendance");
            System.out.println("5. Back");

            int opt = readInt(sc, "Choice: ");

            if (opt == 1) {
                viewEmployees(orgId);
                int empId = readInt(sc, "Employee ID: ");
                String date = readLine(sc, "Date (YYYY-MM-DD): ");
                System.out.println("Status: 1. Present | 2. Absent | 3. Half-Day | 4. Leave");
                int s = readInt(sc, "Choice: ");
                String status = (s == 1) ? "Present" : (s == 2 ? "Absent" : (s == 3 ? "Half-Day" : "Leave"));
                String remarks = readLine(sc, "Remarks: ");

                boolean success = Database.insertAttendance(orgId, empId, date, status, remarks);
                if (success) {
                    System.out.println("Attendance recorded.");
                }
            } else if (opt == 2) {
                int max = 200;
                int[] ids = new int[max];
                int[] empIds = new int[max];
                String[] dates = new String[max];
                String[] statuses = new String[max];
                String[] remarks = new String[max];

                int count = Database.getAttendance(orgId, ids, empIds, dates, statuses, remarks);

                System.out.println("\nID | Employee ID | Date | Status | Remarks");
                System.out.println("-------------------------------------------------");
                if (count == 0) {
                    System.out.println("No records found.");
                } else {
                    for (int i = 0; i < count; i++) {
                        System.out.println(ids[i] + " | " + empIds[i] + " | " + dates[i] + " | " + statuses[i] + " | " + remarks[i]);
                    }
                }
                System.out.println();
            } else if (opt == 3) {
                int attId = readInt(sc, "Attendance ID to update: ");
                System.out.println("New Status: 1. Present | 2. Absent | 3. Half-Day | 4. Leave");
                int s = readInt(sc, "Choice: ");
                String status = (s == 1) ? "Present" : (s == 2 ? "Absent" : (s == 3 ? "Half-Day" : "Leave"));
                String remarks = readLine(sc, "New Remarks: ");

                boolean success = Database.updateAttendance(orgId, attId, status, remarks);
                if (success) {
                    System.out.println("Attendance updated.");
                } else {
                    System.out.println("Record not found.");
                }
            } else if (opt == 4) {
                int attId = readInt(sc, "Attendance ID to delete: ");
                boolean success = Database.deleteAttendance(orgId, attId);
                if (success) {
                    System.out.println("Attendance deleted.");
                } else {
                    System.out.println("Record not found.");
                }
            } else if (opt == 5) {
                break;
            } else {
                System.out.println("Invalid option.");
            }
        }
    }

    private static void salarySection(Scanner sc, int orgId) {
        while (true) {
            System.out.println("\n--- Salary Options ---");
            System.out.println("1. Create Salary");
            System.out.println("2. View Salary");
            System.out.println("3. Update Salary");
            System.out.println("4. Delete Salary");
            System.out.println("5. Back");

            int opt = readInt(sc, "Choice: ");

            if (opt == 1) {
                viewEmployees(orgId);
                int empId = readInt(sc, "Enter Employee ID: ");
                String monthYear = readLine(sc, "Month & Year: ");
                double basic = readDouble(sc, "Basic Salary: ");
                double bonus = readDouble(sc, "Bonus: ");
                double deductions = readDouble(sc, "Deductions: ");

                double netSalary = basic + bonus - deductions;
                if (netSalary < 0) netSalary = 0;

                System.out.println("Net Salary: " + netSalary);
                System.out.println("Status: 1. Paid | 2. Pending");
                int st = readInt(sc, "Choice: ");
                String status = (st == 1) ? "Paid" : "Pending";

                boolean success = Database.insertSalary(orgId, empId, monthYear, basic, bonus, deductions, netSalary, status);
                if (success) {
                    System.out.println("Salary created successfully.");
                }
            } else if (opt == 2) {
                int max = 200;
                int[] ids = new int[max];
                int[] empIds = new int[max];
                String[] months = new String[max];
                double[] basics = new double[max];
                double[] bonuses = new double[max];
                double[] deducts = new double[max];
                double[] nets = new double[max];
                String[] statuses = new String[max];

                int count = Database.getSalaries(orgId, ids, empIds, months, basics, bonuses, deducts, nets, statuses);

                System.out.println("\nID | Emp ID | Month/Year | Basic | Bonus | Deduct | Net | Status");
                System.out.println("------------------------------------------------------------------");
                if (count == 0) {
                    System.out.println("No salary records found.");
                } else {
                    for (int i = 0; i < count; i++) {
                        System.out.println(ids[i] + " | " + empIds[i] + " | " + months[i] + " | " + basics[i] + " | " + bonuses[i] + " | " + deducts[i] + " | " + nets[i] + " | " + statuses[i]);
                    }
                }
                System.out.println();
            } else if (opt == 3) {
                int salId = readInt(sc, "Salary ID to update: ");
                double basic = readDouble(sc, "New Basic: ");
                double bonus = readDouble(sc, "New Bonus: ");
                double deductions = readDouble(sc, "New Deductions: ");
                double net = basic + bonus - deductions;
                if (net < 0) net = 0;

                System.out.println("Status: 1. Paid | 2. Pending");
                int st = readInt(sc, "Choice: ");
                String status = (st == 1) ? "Paid" : "Pending";

                boolean success = Database.updateSalary(orgId, salId, basic, bonus, deductions, net, status);
                if (success) {
                    System.out.println("Salary updated.");
                } else {
                    System.out.println("Record not found.");
                }
            } else if (opt == 4) {
                int salId = readInt(sc, "Salary ID to delete: ");
                boolean success = Database.deleteSalary(orgId, salId);
                if (success) {
                    System.out.println("Salary deleted.");
                } else {
                    System.out.println("Record not found.");
                }
            } else if (opt == 5) {
                break;
            } else {
                System.out.println("Invalid option.");
            }
        }
    }

    private static void noticeHolidaySection(Scanner sc, int orgId) {
        while (true) {
            System.out.println("\n--- Notice / Holiday Options ---");
            System.out.println("1. Create Notice/Holiday");
            System.out.println("2. View Notice/Holiday");
            System.out.println("3. Update Notice/Holiday");
            System.out.println("4. Delete Notice/Holiday");
            System.out.println("5. Back");

            int opt = readInt(sc, "Choice: ");

            if (opt == 1) {
                System.out.println("Type: 1. Notice | 2. Holiday");
                int t = readInt(sc, "Choice: ");
                String type = (t == 1) ? "Notice" : "Holiday";
                String title = readLine(sc, "Title: ");
                String desc = readLine(sc, "Description: ");
                String date = readLine(sc, "Date: ");

                boolean success = Database.insertNoticeHoliday(orgId, type, title, desc, date);
                if (success) {
                    System.out.println(type + " created.");
                }
            } else if (opt == 2) {
                int max = 100;
                int[] ids = new int[max];
                String[] types = new String[max];
                String[] titles = new String[max];
                String[] descs = new String[max];
                String[] dates = new String[max];

                int count = Database.getNoticesHolidays(orgId, ids, types, titles, descs, dates);

                System.out.println("\nID | Type | Date | Title | Description");
                System.out.println("----------------------------------------------");
                if (count == 0) {
                    System.out.println("No notices or holidays found.");
                } else {
                    for (int i = 0; i < count; i++) {
                        System.out.println(ids[i] + " | " + types[i] + " | " + dates[i] + " | " + titles[i] + " | " + descs[i]);
                    }
                }
                System.out.println();
            } else if (opt == 3) {
                int id = readInt(sc, "ID to update: ");
                System.out.println("Type: 1. Notice | 2. Holiday");
                int t = readInt(sc, "Choice: ");
                String type = (t == 1) ? "Notice" : "Holiday";
                String title = readLine(sc, "New Title: ");
                String desc = readLine(sc, "New Description: ");
                String date = readLine(sc, "New Date: ");

                boolean success = Database.updateNoticeHoliday(orgId, id, type, title, desc, date);
                if (success) {
                    System.out.println("Updated.");
                } else {
                    System.out.println("Record not found.");
                }
            } else if (opt == 4) {
                int id = readInt(sc, "ID to delete: ");
                boolean success = Database.deleteNoticeHoliday(orgId, id);
                if (success) {
                    System.out.println("Deleted.");
                } else {
                    System.out.println("Record not found.");
                }
            } else if (opt == 5) {
                break;
            } else {
                System.out.println("Invalid option.");
            }
        }
    }

    private static String readLine(Scanner sc, String prompt) {
        String s = "";
        while (s.trim().isEmpty()) {
            System.out.print(prompt);
            s = sc.nextLine();
        }
        return s.trim();
    }

    private static int readInt(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (Exception e) {
                System.out.println("Invalid number. Try again.");
            }
        }
    }

    private static double readDouble(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            try {
                return Double.parseDouble(line);
            } catch (Exception e) {
                System.out.println("Invalid decimal. Try again.");
            }
        }
    }
}
