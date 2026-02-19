package bg.sofia.uni.fmi.mjt.splitwise.model;

import java.io.Serializable;
import java.util.Set;

public record Group(String name, Set<String> members) implements Serializable {
}