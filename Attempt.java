import java.util.ArrayList;
import java.util.Comparator;

/*
 * An attempt is a student's enrollment in a class
 * This is a helper class for the main program
 */
public class Attempt {
	//tuple for is : <CNO, PNAME, TERM, SIZE, AVERAGE, MARK, PREREQ, SNO>
	
	private String cno = null;
	private String inst = null;
	private String term = null;
	private String csize = null;
	private String avg = null;
	private String mark = null;
	
	private String prereq= null;
	private String sno = null;
	
	private int indent = 0;
	
	private ArrayList<Attempt> prereqList = new ArrayList<Attempt>();
	
	/*
	 * ctor
	 */
	Attempt(ArrayList<String> t) {
		cno=t.get(0);
		inst=t.get(1);
		term=t.get(2);
		csize=t.get(3);
		avg=t.get(4);
		mark=t.get(5);
		prereq=t.get(6);
		sno=t.get(7);
	}
	
	/*
	 * Useful methods
	 */
	

	// Recursion - check if a is a prereq of me
	public boolean findPlace(Attempt a) {
		if (inst == null) {
			return false;
		}
		String[] ps = prereq.split(",");
		for (String p : ps) {
			if (p.equals(a.getCNO())){
				this.prereqList.add(a);
				return true;
			}
		}
		//if not, look in my kids
		if (prereqList.isEmpty()) {
			return false;
		}
		return this.lookInMyKids(a);
	}
	// Recursion - check if a is in my kids
	public boolean lookInMyKids(Attempt a) {
		for (Attempt t: prereqList) {
			if(t.findPlace(a)) {
				return true;
			}
		}
		return false;
	}
	
	// Recursion - print this object
	public void printME() {
		// figure out how many places to indent
		if (this.indent == 0) {
			System.out.print("Course#: ");
		} else {
			for (int i = 0; i < this.indent; i++) {
				System.out.print("\t");
			}
			System.out.print("Prerequisites: ");
		}
		
		System.out.print(cno + " Instructor:" + inst + 
				" Term:" + term + " Class size:" + csize + 
				" Class Avg:" + avg + " Mark:" + mark);
		System.out.println("");
		if (prereqList.isEmpty()) {
			return;
		}
		//deploy this on my kids as well
		this.printKids();
	}
	
	// Recursion - call printME() on all of my kids
	public void printKids() {
		for (Attempt t: prereqList) {
			t.printME();
		}
	}
	
	// Recursion - add indent level
	public void setIndent(int myLevel) {
		this.indent = myLevel;
		if (prereqList.isEmpty()) {
			return;
		}
		int kidLevel = myLevel + 1;
		this.kidsIndent(kidLevel);
	}
	
	// Recursion - call addIndent to all my kids
	public void kidsIndent(int myLevel) {
		for (Attempt t: prereqList) {
			t.setIndent(myLevel);
		}
	}
	/*
	 * get methods
	 */
	public String getCNO() {
		return this.cno;
	}
	public String getPrereq() {
		return this.prereq;
	}
	
	public String getINST() {
		return this.inst;
	}
	
	public String getTERM() {
		return this.term;
	}
	/*
	 * set methods
	 */
	public void setcno(String s) {
		this.cno = s;
	}
	
	public void setinst(String s) {
		this.inst = s;
	}
	
	public void setterm(String s) {
		this.term = s;
	}
	
	public void csize(String s) {
		this.csize = s;
	}
	
	public void setavg(String s) {
		this.avg = s;
	}
	
	public void setmark(String s) {
		this.mark = s;
	}
	
	public void addPrereq(String s) {
		this.prereq = this.prereq + "," + s;
	}
	

	/*
	 * Other functions - for debugging
	 */
	public void printAttempt() {
		System.out.println(cno + " " + inst + " " + term + " " + csize + " " + avg + " " + mark + " " + prereq);
	}
}

//for sorting roots
class AttemptCNOComparator implements Comparator<Attempt> {
	public int compare(Attempt a1, Attempt a2) {
		int c1 = Integer.parseInt(a1.getCNO().replaceAll("[^0-9]", ""));
		int c2 = Integer.parseInt(a2.getCNO().replaceAll("[^0-9]", ""));
		return c2 - c1;
	}
}
