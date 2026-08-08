package it.unibo.antsim.agent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.NoSuchElementException;

/**
 * This class manages a collection of ants, coordinating their decision and movement.
 */
public class AntGroup {
    private final List<Ant> ants;

    /**
     * Instantiates a new Ant group.
     */
    public AntGroup() {
        this.ants = new ArrayList<>();
    }

    /**
     * Add ant in the group.
     *
     * @param ant the ant to add
     */
    public void addAnt(final Ant ant) {
        this.ants.add(Objects.requireNonNull(ant));
    }

    /**
     * Get the list of ants.
     *
     * @return an unmodifiable list of ants in the group
     */
    public List<Ant> getAnts() {
        return Collections.unmodifiableList(ants);
    }

    /**
     * Get the number od ants in the group.
     *
     * @return the total number of ants
     */
    public int size() {
        return ants.size();
    }

    /**
     * This one clear the list by removing all ants in the group.
     */
    public void clear() {
        ants.clear();
    }

    /**
     * Removes the last element added to the group.
     */
    public void removeLast() {
        if (ants.isEmpty()) {
            throw new NoSuchElementException("Cannot remove from an empty group");
        }
        ants.removeLast();
    }
}
