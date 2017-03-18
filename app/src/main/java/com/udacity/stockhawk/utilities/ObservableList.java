package com.udacity.stockhawk.utilities;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.UnaryOperator;

import rx.Observable;
import rx.subjects.BehaviorSubject;

public class ObservableList<E> implements List<E> {
    private final BehaviorSubject<E> addSubject = BehaviorSubject.create();
    private final BehaviorSubject<E> removeSubject = BehaviorSubject.create();
    private final List<E> list;


    public static <E> ObservableList<E> create() {
        return new ObservableList<>(new ArrayList<>());
    }

    public static <E> ObservableList<E> create(List<E> list) {
        return new ObservableList<>(list);
    }

    public ObservableList(List<E> list) {
        this.list = list;
    }

    @Override
    public boolean add(E element) {
        if (list.add(element)) {
            addSubject.onNext(element);
            return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean remove(Object object) {
        if (list.remove(object)) {
            removeSubject.onNext((E) object);
            return true;
        }
        return false;
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return list.contains(o);
    }

    @NonNull
    @Override
    public Iterator<E> iterator() {
        return list.iterator();
    }

    @NonNull
    @Override
    public Object[] toArray() {
        return list.toArray();
    }

    @NonNull
    @Override
    public <T> T[] toArray(@NonNull T[] a) {
        return list.toArray(a);
    }

    @Override
    public boolean containsAll(@NonNull Collection<?> c) {
        return list.containsAll(c);
    }

    @Override
    public boolean addAll(@NonNull Collection<? extends E> c) {
        return list.addAll(c);
    }

    @Override
    public boolean addAll(int index, @NonNull Collection<? extends E> c) {
        return list.addAll(index, c);
    }

    @Override
    public boolean removeAll(@NonNull Collection<?> c) {
        return list.removeAll(c);
    }

    @Override
    public boolean retainAll(@NonNull Collection<?> c) {
        return list.retainAll(c);
    }

    @Override
    public void clear() {
        list.clear();
    }

    @Override
    public boolean equals(Object o) {
        return list.equals(o);
    }

    @Override
    public int hashCode() {
        return list.hashCode();
    }

    @Override
    public E get(int index) {
        return list.get(index);
    }

    @Override
    public E set(int index, E element) {
        return list.set(index, element);
    }

    @Override
    public void add(int index, E element) {
        list.add(index, element);
    }

    @Override
    public E remove(int index) {
        return list.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return list.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return list.lastIndexOf(o);
    }

    @Override
    public ListIterator<E> listIterator() {
        return list.listIterator();
    }

    @NonNull
    @Override
    public ListIterator<E> listIterator(int index) {
        return list.listIterator(index);
    }

    @NonNull
    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        return list.subList(fromIndex, toIndex);
    }

    @Override
    public Spliterator<E> spliterator() {
        return list.spliterator();
    }

    @Override
    public void replaceAll(UnaryOperator<E> operator) {
        list.replaceAll(operator);
    }

    @Override
    public void sort(Comparator<? super E> c) {
        list.sort(c);
    }

    public Observable<Pair<ChangeType, E>> getUpdateObservable() {
        return addSubject.map(element -> Pair.create(ChangeType.ADD, element)).
                mergeWith(removeSubject.map(element -> Pair.create(ChangeType.REMOVE, element)));
    }

    public Observable<E> getAddObservable() {
        return addSubject;
    }

    public Observable<E> getRemoveObservable() {
        return removeSubject;
    }
}