package model;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

// Cart: Map<artworkId, Artwork>. Ek artwork ek hi baar aa sakta hai (unique piece)
public class Cart {
    private final Map<Integer, Artwork> items = new LinkedHashMap<>();

    public boolean add(Artwork a) { return items.putIfAbsent(a.getId(), a) == null; }
    public void remove(int artworkId) { items.remove(artworkId); }
    public void clear() { items.clear(); }
    public boolean isEmpty() { return items.isEmpty(); }
    public int size() { return items.size(); }
    public Collection<Artwork> getItems() { return items.values(); }
    public double total() { return items.values().stream().mapToDouble(Artwork::getPrice).sum(); }
}
