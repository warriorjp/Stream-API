public interface Prototype extends Cloneable {
    Prototype clone();
}

public class Document implements Prototype {
    private String title;
    private String content;

    public Document(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public Document clone() {
        return new Document(this.title, this.content);
    }

    public void print() {
        System.out.println("Title: " + title);
        System.out.println("Content: " + content);
    }
}

public class Main {
    public static void main(String[] args) {
        // Original document
        Document doc1 = new Document("Resume", "This is my resume.");
        doc1.print();

        System.out.println("\nCloning document...\n");

        // Clone document
        Document doc2 = doc1.clone();
        doc2.setTitle("Resume - Copy");
        doc2.print();
    }
}
