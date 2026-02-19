package bg.sofia.uni.fmi.mjt.splitwise.command.commands;

import java.util.Optional;
import java.util.stream.Stream;

public class test {
    public void main() {
        Optional<Integer> i = Stream.of(1, 2, 3, 4, 5, 6, 7, 8)
            .filter(x -> {
                System.out.println("Filtering: " + x);
                return x < 3;
            })
            .map(x -> {
                System.out.println("Mapping: " + x);
                return x * 2;
            })
            .findFirst();

        System.out.println("Result: " + i.orElse(-1));
    }
}
