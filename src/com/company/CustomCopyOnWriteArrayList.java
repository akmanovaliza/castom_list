package com.company;

import java.util.*;
import java.util.function.Consumer;

public class CustomCopyOnWriteArrayList {

	private Integer[] elementData;
	private int modCount = 0;
	private int size = 0;
	private Map<Itr, Map<Integer, Integer>> deletedItems = new HashMap<>();
	private List<Itr> createdItr = new ArrayList<>();

	public CustomCopyOnWriteArrayList() {
		this.elementData = new Integer[10];
	}

	public int size() {
		return size;
	}

	public boolean isEmpty() {
		return size == 0;
	}

	public Iterator<Integer> iterator() {
		final Itr itr = new Itr();
		createdItr.add(itr);
		return itr;
	}

	public boolean add(Integer e) {
		elementData[size++] = e;
		modCount++;
		return true;
	}

	public Integer get(int index) {
		if (index >= size) {
			throw new IllegalArgumentException();
		}
		return elementData[index];
	}

	public Integer set(int index, Integer element) {
		if (size <= index) {
			elementData[index] = element;
			size = index + 1;
			modCount++;
			size++;
			return element;
		}
		final Integer val = elementData[index];
		for (Itr itr : createdItr) {
			if (deletedItems.containsKey(itr)) {
				deletedItems.get(itr).put(index, val);
			} else {
				Map<Integer, Integer> map = new HashMap<>();
				map.put(index, val);
				deletedItems.put(itr, map);
			}
		}
		modCount++;
		elementData[index] = element;
		return element;
	}

	public Integer remove(int index) {
		if (index >= size) {
			throw new IllegalArgumentException();
		}
		final Integer val = elementData[index];
		for (Itr itr : createdItr) {
			if (deletedItems.containsKey(itr)) {
				deletedItems.get(itr).put(index, val);
			} else {
				Map<Integer, Integer> map = new HashMap<>();
				map.put(index, val);
				deletedItems.put(itr, map);
			}
		}
		final Integer[] integers1 = Arrays.copyOf(elementData, index);
		final Integer[] integers2 = Arrays.copyOfRange(elementData, index + 1, size);
		List<Integer> resultList = new ArrayList<>();
		Collections.addAll(resultList, integers1);
		Collections.addAll(resultList, integers2);
		elementData = resultList.toArray(elementData);
		modCount++;
		size--;
		return val;
	}

	private class Itr implements Iterator<Integer> {

		int cursor = 0;
		int expectedModCount = modCount;
		int size = size();

		public boolean hasNext() {
			final boolean b = cursor != size();
			if (!b) {
				deletedItems.remove(this);
				createdItr.remove(this);
			}
			return b;
		}

		@SuppressWarnings("unchecked")
		public Integer next() {
			int i = cursor;
			if (i >= size - 1) {
				throw new NoSuchElementException();
			}
			cursor = i + 1;
			final Map<Integer, Integer> map = deletedItems.get(this);
			if (map != null) {
				return map.getOrDefault(i, elementData[i]);
			}
			if (cursor == size - 1) {
				deletedItems.remove(this);
				createdItr.remove(this);
			}
			return elementData[i];
		}

		@Override
		public void forEachRemaining(Consumer<? super Integer> action) {
			Objects.requireNonNull(action);
			int i = cursor;
			if (i < size) {
				final Object[] es = elementData;
				if (i >= es.length)
					throw new ConcurrentModificationException();
				for (; i < size; i++) {
					final Map<Integer, Integer> map = deletedItems.get(this);
					if (map != null) {
						action.accept(map.getOrDefault(i, elementData[i]));
					} else {
						action.accept(elementData[i]);
					}
				}
				cursor = i;
				deletedItems.remove(this);
				createdItr.remove(this);
			}
		}

		@Override
		public int hashCode() {
			return size * 1325 + expectedModCount * 361;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Itr itr) {
				return this.size == itr.size && this.expectedModCount == itr.expectedModCount;
			}
			return false;
		}

	}

}
