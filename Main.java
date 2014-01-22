import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;

public class Main {
	static final String JDBC_DRIVER = "com.ibm.db2.jcc.DB2Driver";
    static final String USER = "db2guest";
    static final String PASS = "wisphartproudthrill";
    static final String DB_URL = "jdbc:db2://linux028.student.cs.uwaterloo.ca:50002/cs348";
    private static ArrayList<Attempt> roots = new ArrayList<Attempt>();
       
    /*
     * final function to print roots
     */
    public static void printRoots() {
    	for(Attempt t: roots) {
    		t.printME();
    	}
    }
    /*
     * uses recursion to place each enrollment tuple in the correct format
     */
    public static void formatRoots() {
    	int count = roots.size();
    	
    	/*
    	 * 1. Check if we are missing any prereqs (should insert a null Attempt object)
    	 */
    	
    	for (int i = 0; i < count; i++) {
    		Attempt a = roots.get(0);
    		roots.remove(0);
    		boolean foundPre = false;
    		for (Attempt t : roots) {
    			if (t.getCNO().equals(a.getPrereq())) {
    				foundPre = true;
    				break;
    			}
    			
    		}
    		
    		if (!foundPre) {
    			ArrayList<String> blank = new ArrayList<String>();
    			blank.add(a.getPrereq());	
    			for (int j= 1; j<8; j++) {
    				blank.add(null);
    			}
    			Attempt b = new Attempt(blank);
    			roots.add(b);
    		}
    		roots.add(a);
    	}

    	/*
    	 * 2. clean up tuple so that each course only has one entry
    	 */
    	count = roots.size();
    	for (int i = 0; i < count; i++) {
    		Attempt a = roots.get(0);
    		roots.remove(0);
    		int foundSame = 0;
    		for (Attempt t : roots) {
    			// same class has multiple prereqs
    			if (a.getCNO().equals(t.getCNO()) && 
    				a.getINST().equals(t.getINST()) &&
    				a.getTERM().equals(t.getTERM()) &&
    				!a.getPrereq().equals(t.getPrereq())) {
    				
    				t.addPrereq(a.getPrereq());
    				foundSame++;
    			}
    		}
    		if (foundSame == 0) {
    			roots.add(a);
    		}
    	}
	
    	
    	/*
    	 * 3. Sort the whole list
    	 * Sorting Strategy:
    	 * 1. pop item off
    	 * 2. Use Recursion: find out where to insert it in roots
    	 */
    	count=roots.size();
    	for (int i = 0; i < count; i++){
    		Attempt a = roots.get(0);
    		roots.remove(0);
    		    		
    		// now figure out where to insert a in roots
    		boolean foundPlace = false;
    		for(Attempt t: roots) {
    			if (t.findPlace(a)) {
    				foundPlace = true;
    				break;
    			}
    		}
    		if (!foundPlace) {// this must be a root class add it back to roots
    			roots.add(a);
    		}
    		
    	}// where to insert each tuple in rs
    	
    	
		
		/*
		 * 4. insert indentation information (now many spaces to indent
		 */
    	for (Attempt t: roots) {
    		t.setIndent(0);
    	}
    	/*
    	 * 5. sort roots in desc CNO order
    	 */
    	List<Attempt> r = roots;
    	Collections.sort(r, new AttemptCNOComparator());
    	//Collections.sort(r, new AttemptCNOComparator());
    	/*
    	 * 6. finally, print
    	 */
    	printRoots();
    }
    
    /*
     * Executes query and populates roots
     */
    public static void parseStudent(Statement stmt, String sid) {
    	
    	try {
    		// PRINT FIRST LINE (SNO, SNAME)
        	String student = "SELECT enrollment.Student.SNAME "
        			+ "FROM enrollment.Student "
        			+ "WHERE enrollment.Student.SNO='"+sid+"'";
        	
        	ResultSet rs = stmt.executeQuery(student);
        	rs.next();

        	System.out.println("Student #: " + sid + 
        			" Student Name: "+rs.getString(1));

        	
    		// BUILD THE QUERY
    		//1. get count and average of each class
            String stat = "SELECT CNO, TERM, SECTION, "
            					+ "COUNT(DISTINCT SNO) AS SIZE, "
            					+ "AVG(MARK) AS AVERAGE "
            		+ "FROM enrollment.Enrollment "
            		+ "GROUP BY enrollment.Enrollment.CNO, "
            				+ "enrollment.Enrollment.TERM, "
            				+ "enrollment.Enrollment.SECTION";
            
            //2. Basic information for each class that student has attended
            String info = "SELECT basicInfo.SNO, basicInfo.CNO, "
            					+ "enrollment.Professor.PNAME, "
            					+ "basicInfo.TERM, basicInfo.MARK, "
            					+ "basicInfo.SECTION "
            		+ "FROM "
	            		+ "(SELECT SNO, enrollment.Enrollment.CNO, "
	            				+ "enrollment.Class.INSTRUCTOR, "
	            				+ "enrollment.Enrollment.TERM, "
	            				+ "enrollment.Enrollment.SECTION, "
	            				+ "enrollment.Enrollment.MARK "
	            		+ "FROM "
	                    + "enrollment.Enrollment INNER JOIN enrollment.Class "
	                    + "ON enrollment.Enrollment.CNO = enrollment.Class.CNO AND "
	                    + "enrollment.Enrollment.TERM = enrollment.Class.TERM AND "
	                    + "enrollment.Enrollment.SECTION = enrollment.Class.SECTION "
	                    + "WHERE "
	                    + "enrollment.Enrollment.SNO='"+sid+"')as basicInfo "
                    + "LEFT OUTER JOIN enrollment.Professor "
                    + "ON basicInfo.INSTRUCTOR = enrollment.Professor.EID";
            
            //3. Join [1] and [2] to put information about each class together.
            String taken = "SELECT info.CNO, info.PNAME, info.TERM, stat.SIZE, "
            					+ "stat.AVERAGE, info.MARK "
            		+ "FROM "
            			+ "("+info+") AS info "
            		+ "INNER JOIN "
            			+ "("+stat+") AS stat "
            		+ "ON info.CNO = stat.CNO AND info.TERM = stat.TERM "
            			+ "AND info.SECTION = stat.SECTION";
    		
            //4. Join [3] with Prerequisite to get all the prereqs for each enrollment
            String sql = "SELECT taken.*, enrollment.Prerequisite.PREREQ "
            		+ "FROM"
            			+ "("+taken+") as taken "
            		+ "LEFT OUTER JOIN enrollment.Prerequisite "
            		+ "ON taken.CNO=enrollment.Prerequisite.CNO ";
            
            /*
             * 5. execute: tuple will be in the form of:
             * <CNO, PNAME, TERM, SIZE, AVERAGE, MARK, PREREQ, SNO>
             */
            rs = stmt.executeQuery(sql);
    		
    		
 
    		//POPULATE roots AND FORMAT OUTPUT
    		int columns = rs.getMetaData().getColumnCount();
    
    		while (rs.next()){ // push tuple into my roots array list
    			
    			ArrayList<String> tuple = new ArrayList<String>();
    			
    			for (int i = 1; i<= columns; i++) {
    				tuple.add(rs.getString(i));
    			}
    			tuple.add(sid);
    			Attempt a = new Attempt(tuple);
    			roots.add(a);
    		}
    		formatRoots();
  
    		
    	}catch(SQLException se) {
    		se.printStackTrace();
    		System.exit(1);
    	}
    }
    public static void main(String[] args) {
    	try {
    		// 1. DB connections and set up
            Class.forName("com.ibm.db2.jcc.DB2Driver");
            
            Connection con = DriverManager.getConnection(DB_URL, USER, PASS);
            Statement stmt=con.createStatement();
            
            //2. get ready for user input
            BufferedReader cl = new BufferedReader(new InputStreamReader(System.in));
            String input = null;
            
            while (true) {
            	System.out.println("Enter Student ID or exit to quit: ");
            	input = cl.readLine();
            	
            	//process input
            	if (input.equals("")) {
            		continue;            		
            	}else if (input.equals("exit")) {
            		System.exit(0);
            	} else {
            		roots = new ArrayList<Attempt>();
            		parseStudent(stmt, input);
            	}
            } // while command line loop
            
    	} catch(IOException ioe) {
    		ioe.getStackTrace();
    		System.out.println("IO error command line! Exiting!");
    		System.exit(1);
    	}catch(ClassNotFoundException ce) {
            System.out.println("Could not load DB2 driver!");
            ce.printStackTrace();
    
    	}catch(Exception e) {
            e.printStackTrace();
    	} // outer trycatch

    }
}
