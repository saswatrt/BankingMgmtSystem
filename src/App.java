import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;
import credentials.Credentials;


public class App{
    static Scanner sc=new Scanner(System.in);
    public static void main(String[] args) throws Exception {
        Connection con=null;
        try{
            String url="jdbc:oracle:thin:@Saswat:1521:xe";
            con=DriverManager.getConnection(url,Credentials.getUser(),Credentials.getPassword());
            //con.setAutoCommit(false);
            System.out.println(con.getAutoCommit());
            int choice;
            //===============Menu=======================
            do{
                System.out.println("Welcome To Bank");
                System.out.println("1.Display All Customer Records");
                System.out.println("2.Add Customer Record");
                System.out.println("3.Delete Customer Record");
                System.out.println("4.Update Customer Record");
                System.out.println("5.Show Account Details");
                System.out.println("6.Display Loan Details");
                System.out.println("7.Deposit Money");
                System.out.println("8.Withdraw Money");
                System.out.println("0.Exit");
                System.out.println("Enter choice:");
                choice=sc.nextInt();
                sc.nextLine();
                switch (choice){
                case 1:
                    displayCustomer(con);
                    break;
                case 2:
                    insertCustomer(con);
                    displayCustomer(con);
                    break;
                case 3:
                    deleteCustomer(con);
                    displayCustomer(con);
                    break;
                case 4:
                    updateCustomer(con);
                    displayCustomer(con);
                    break;
                case 5:
                    displayCustomerAccountBranch(con);
                    break; 
                case 6:
                    displayLoanDetails(con);
                    break;
                case 7:
                    depositMoney(con);
                    System.out.println("Verifying Details...");
                    displayCustomerAccountBranch(con);
                    break;
                case 8:
                    withdrawMoney(con);
                    System.out.println("Verifying Details...");
                    displayCustomerAccountBranch(con);
                    break;
                default:
                    System.out.println("Invalid Input");
                }
            }while(choice>0);
        }
        catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            try {
                if (con != null) con.rollback(); // Rollback on failure
                System.out.println("Transaction rolled back.");
            } 
            catch (SQLException rollbackEx) {
                System.err.println("Rollback failed: " + rollbackEx.getMessage());
            }
        } 
        catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        } 
        finally {
            try {
                if (con != null && !con.isClosed()) {
                    con.close();
                    System.out.println("Connection closed.");
                }
            } 
            catch (SQLException e) {
                System.err.println("Failed to close connection: " + e.getMessage());
            }
        }
    }
    public static void display(ResultSet rs) throws Exception{
        try{
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            int colWidth[]=new int[columnCount];//The max width needed for any column,for better formatting
            for(int i=1;i<=columnCount;i++){
                String colName=metaData.getColumnLabel(i);//Column name, first column is at i=1
                int size=metaData.getColumnDisplaySize(i);
                if(size>colName.length())   colWidth[i-1]=size+2;
                else colWidth[i-1]=colName.length()+2;
                System.out.printf("%-"+(colWidth[i-1])+"s",colName);
            }
            System.out.println();
            for(int width:colWidth){
                System.out.print("-".repeat(width));
            }
            System.out.println();
            while(rs.next()){
                for(int i=1;i<=columnCount;i++){                   
                        String out=rs.getString(i);
                        System.out.printf("%-"+(colWidth[i-1])+"s",out);
                }
                System.out.println();
            }
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
    public static void displayCustomer(Connection con) throws Exception{
        try{
            Statement stmt=con.createStatement();
            String qr="select * from Customer";
            ResultSet rs=stmt.executeQuery(qr);
            display(rs);
            stmt.close();
        }
        catch(SQLException e){
            System.err.println(e);
        }
    }
    public static void insertCustomer(Connection con) throws Exception{
        try{
            System.out.println("Enter customer No.(Like C----)");
            String cust_no=sc.next();
            System.out.println("Enter customer name");
            sc.nextLine();
            String name=sc.nextLine();
            System.out.println("Enter customer PhoneNo.");
            String phno=sc.next();
            System.out.println("Enter customer City");
            String city=sc.next();
            long ph=Long.parseLong(phno);
            PreparedStatement ps=con.prepareStatement("insert into customer values(?,?,?,?)");
            ps.setString(1, cust_no);
            ps.setString(2,name);
            ps.setLong(3,ph);
            ps.setString(4,city);
            ps.executeUpdate();//executeQuery() implicitly calls auto commit 
            ps.close();
        }
        catch(Exception e){
            System.err.println(e.getMessage());
            con.rollback();
        }
    }
    public static void deleteCustomer(Connection con) throws Exception{
        try{
            System.out.println("Enter Customer No.");
            String cust_no=sc.next();
            PreparedStatement ps=con.prepareStatement("delete from customer where cust_no=?");
            ps.setString(1, cust_no);
            ps.executeUpdate();
        }
        catch (Exception e){
            System.err.println(e.getMessage());
        }
    }
    public static void updateCustomer(Connection con) throws Exception{
        //there was some problem with executeUpdate(),it got solved by closing the connection using ^z during insert record program run
        PreparedStatement ps=null;
        try{
            System.out.println("Enter Customer No.");
            String cust_no=sc.next();
            
            System.out.println("1:Update name\n"+"2:Update Phoneno.\n"+"3:Update city");
            char cont;
            do{
                System.out.println("Enter Choice:");
                int ch=sc.nextInt();
                sc.nextLine();
                switch (ch){
                    case 1:
                        System.out.println("Enter Updated Name:");
                        String name=sc.nextLine();
                        ps=con.prepareStatement("update customer set name = ? where cust_no=?");
                        ps.setString(1,name);
                        ps.setString(2,cust_no);
                        int rowChanged=ps.executeUpdate();
                        System.out.println(rowChanged+" Rows Updated...");
                        break;
                    case 2:
                        System.out.println("Enter Updated PhoneNo:");
                        Long phno=sc.nextLong();
                        ps=con.prepareStatement("update customer set Phone_no = ? where cust_no=?");
                        ps.setLong(1,phno);
                        ps.setString(2,cust_no);
                        rowChanged=ps.executeUpdate();
                        System.out.println(rowChanged+" Rows Updated...");
                        break;
                    case 3:
                        System.out.println("Enter Updated City:");
                        String city=sc.next();
                        sc.nextLine();
                        ps=con.prepareStatement("update customer set city = ? where cust_no=?");
                        ps.setString(1,city);
                        ps.setString(2,cust_no);
                        rowChanged=ps.executeUpdate();
                        System.out.println(rowChanged+" Rows Updated...");
                        break;
                    default:
                        System.out.println("Invalid Input");
                }
                System.out.println("Continue?(y/n)");
                cont=sc.next().charAt(0);
            }while(cont!='n');
            
            if(ps!=null)    ps.close();
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
    public static void displayCustomerAccountBranch(Connection con){
        PreparedStatement ps=null;
        try{
            System.out.println("Enter the Customer No.");
            String cust_no =sc.next();
            sc.nextLine();
            ps=con.prepareStatement("select * from (select * from customer where cust_no=?) natural join (select * from depositor where cust_no=?) natural join (select * from account) natural join (select * from branch)");
            ps.setString(1, cust_no);
            ps.setString(2, cust_no);
            ResultSet rs=ps.executeQuery();
            display(rs);
            ps.close();
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
    public static void displayLoanDetails(Connection con){
        try{
            PreparedStatement ps=con.prepareStatement("select * from (select * from customer where cust_no=?) cross join (select loan_no,amount,branch_code from loan where cust_no=?) natural join(select * from branch where branch_code in (select branch_code from loan where cust_no=?))");
            System.out.println("Enter Customer No.");
            String cust_no=sc.next();
            sc.nextLine();
            ps.setString(1,cust_no);
            ps.setString(2,cust_no);                
            ps.setString(3,cust_no);     
            ResultSet rs=ps.executeQuery();
            display(rs);               
            ps.close();
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
    public static void depositMoney(Connection con){
        try{
            PreparedStatement ps=con.prepareStatement("update Account set balance=balance+? where account_no=?" );
            System.err.println("Enter Account_No");
            String acc_no=sc.next();
            sc.nextLine();
            System.out.println("Enter Amount to deposit");
            long amt=sc.nextLong();
            if(amt>0){
                ps.setLong(1, amt);
                ps.setString(2,acc_no);
                ps.executeUpdate();
            }
            else System.out.println("Invalid Amount");
            ps.close();
        }
        catch(Exception e){
            System.err.println(e.getMessage());
        }
    }
    public static void withdrawMoney(Connection con){
        try{
            PreparedStatement ps=con.prepareStatement("update Account set balance=balance-? where account_no=?" );
            System.err.println("Enter Account_No");
            String acc_no=sc.next();
            sc.nextLine();
            System.out.println("Enter Amount to Withdraw");
            long amt=sc.nextLong();
            if(amt>0){
                PreparedStatement ps1=con.prepareStatement("select balance from account where account_no=?");
                ps1.setString(1, acc_no);
                ResultSet rs=ps1.executeQuery();
                rs.next();
                if(rs.getLong(1)>=amt){
                    ps.setLong(1, amt);
                    ps.setString(2,acc_no);
                    ps.executeUpdate();
                }
                else System.out.println("Amount is more than Balance");
                
            }
            else System.out.println("Invalid Amount");
            ps.close();
        }
        catch(Exception e){
            System.err.println(e.getMessage());
        }
    }
}

