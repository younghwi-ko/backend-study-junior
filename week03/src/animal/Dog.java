package animal;

public class Dog extends Animal {
    private final String breed;

    public Dog(String name, int age, String breed) {
        super(name, age);
        this.breed = breed;
    }

    @Override
    public void greet() {
        super.greet();
        System.out.println("Woof! I am a " + breed + ".");
    }
}
