public class Data {
    private String subject;
    private String teacher;
    private int ID;

    public Data() {
        subject = "";
        teacher = "";
        ID = -1;
    }

    public Data(String pSubject, String pTeacher, int pID) {
        subject = pSubject;
        teacher = pTeacher;
        ID = pID;
    }

    public String getSubject() {
        return(subject);
    }

    public String getTeacher() {
        return(teacher);
    }

    public int getID() {
        return ID;
    }
}
