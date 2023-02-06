import model.SymbolItem;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class SymbolTable {
    private static final List<SymbolTable> allInstances = new LinkedList<>();
    public SymbolTable parent;

    private final String name;
    private final int scopeNumber;
    private final HashMap<String, SymbolItem> table = new HashMap<>();
    private int maxKeyLen = 0;
    private int maxValueLen = 0;

    public SymbolTable(String name, int scopeNumber, SymbolTable parent) {
        this.name = name;
        this.scopeNumber = scopeNumber;
        this.parent = parent;
        allInstances.add(this);
    }

    public static List<SymbolTable> getAllInstances() {
        return allInstances;
    }

    public void insert(String key, SymbolItem value){
        table.put(key, value);
        maxKeyLen = Math.max(maxKeyLen, key.length() + 1);
        maxValueLen = Math.max(maxValueLen, value.toString().length() + 1);
    }

    public SymbolItem lookup(String key){
        return table.getOrDefault(key, null);
    }

    public boolean contains(String key) {
        return table.containsKey(key);
    }

    private String printItems(){
        String tableBorder = '+' + "-".repeat(maxKeyLen+1) + '+' + "-".repeat(maxValueLen+1) + '+' + '\n';

        StringBuilder tableString = new StringBuilder(tableBorder)
                .append("| ")
                .append(String.format("%-" + maxKeyLen + "s", "KEY"))
                .append("| ")
                .append(String.format("%-" + maxValueLen + "s", "VALUE"))
                .append("|")
                .append('\n')
                .append(tableBorder);

        for(var entry: table.entrySet()){
            String key = entry.getKey();
            String value = entry.getValue().toString();
            key = String.format("%-" + maxKeyLen + "s", key);
            value = String.format("%-" + maxValueLen + "s", value);
            tableString.append("| " + key + "| " + value + "|").append('\n');
        }
        tableString.append(tableBorder);

        return tableString.toString();
    }

    @Override
    public String toString() {
        return "=".repeat(30) + " " + name + ": " + scopeNumber + ' ' + "=".repeat(30) + '\n' +
                (table.isEmpty() ? " - empty symbol table\n" : printItems()) + '\n'
                ;
    }

}