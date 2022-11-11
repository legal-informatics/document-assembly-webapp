package legal.documentassembly.util;

import java.io.PrintWriter;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.impl.NTripleWriter;

public class MyNTripleWriter extends NTripleWriter {

    public void write(Model model, String filename) {
        try {
            PrintWriter writer = null;
            try {
                writer = new PrintWriter(filename, "UTF-8");

                StmtIterator iter = model.listStatements();
                Statement stmt = null;
                while (iter.hasNext()) {
                    stmt = iter.nextStatement();
                    //System.out.println("stmt: " + stmt);
                    writeResource(stmt.getSubject(), writer);
                    writer.print(" ");
                    writeResource(stmt.getPredicate(), writer);
                    writer.print(" ");
                    myWriteNode(stmt.getObject(), writer);
                    writer.println(" .");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            writer.flush();
            writer.close();
        } catch (Exception ioe) {
            ioe.printStackTrace();
        }
    }

    private void myWriteNode(RDFNode n, PrintWriter writer) {
        if ((n instanceof Literal)) {
            myWriteLiteral((Literal) n, writer);
        } else {
            writeResource((Resource) n, writer);
        }
    }

    protected void myWriteLiteral(Literal l, PrintWriter writer) {
        String s = l.getString();

        writer.print('"');
        myWriteString(s, writer);
        writer.print('"');
        String lang = l.getLanguage();
        if ((lang != null) && (!lang.equals(""))) {
            writer.print("@" + lang);
        }
        String dt = l.getDatatypeURI();
        if ((dt != null) && (!dt.equals(""))) {
            writer.print("^^<" + dt + ">");
        }
    }

    private void myWriteString(String s, PrintWriter writer) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if ((c == '\\') || (c == '"')) {
                writer.print('\\');
                writer.print(c);
            } else if (c == '\n') {
                writer.print("\\n");
            } else if (c == '\r') {
                writer.print("\\r");
            } else if (c == '\t') {
                writer.print("\\t");
            } else if ((c >= ' ') && (c < '')) {
                writer.print(c);
            } else {
                writer.print(c);
            }
        }
    }
}
