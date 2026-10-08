package animal;

public class Cat extends Animal {
    private final String color;

    public Cat(String name, int age, String color) {
        super(name, age);
        this.color = color;
    }

    @Override
    public void greet() {
        super.greet();
        System.out.println("Meow! My color is " + color + ".");
    }
}
