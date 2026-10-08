package club;

public class Club {
    private final long id;
    private final String name;

    public Club(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() { return id; }
    public String getName() { return name; }
}
