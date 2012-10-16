/*
 *  ProtocolLib - Bukkit server library that allows access to the Minecraft protocol.
 *  Copyright (C) 2012 Kristian S. Stangeland
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms of the 
 *  GNU General Public License as published by the Free Software Foundation; either version 2 of 
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
 *  02111-1307 USA
 */

package com.comphenix.protocol.injector.player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.google.common.base.Objects;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * Represents an array list that wraps another list, while automatically replacing one element with another.
 * <p>
 * The replaced elements can be recovered. 
 * 
 * @author Kristian
 * @param <TKey> - type of the elements we're replacing.
 */
class ReplacedArrayList<TKey> extends ArrayList<TKey> {
	/**
	 * Generated by Eclipse.
	 */
	private static final long serialVersionUID = 1008492765999744804L;
	
	private BiMap<TKey, TKey> replaceMap = HashBiMap.create();
	private List<TKey> underlyingList;
	
	public ReplacedArrayList(List<TKey> underlyingList) {
		this.underlyingList = underlyingList;
	}
	
	/**
	 * Invoked when a element inserted is replaced.
	 * @param inserting - the element inserted.
	 * @param replacement - the element that it should replace.
	 */
	protected void onReplacing(TKey inserting, TKey replacement) {
		// Default is to do nothing.
	}
	
	/**
	 * Invoked when an element is being inserted.
	 * <p>
	 * This should be used to add a "replace" map.
	 * @param inserting - the element to insert.
	 */
	protected void onInserting(TKey inserting) {
		// Default is again nothing
	}
	
	/**
	 * Invoksed when an element is being removed.
	 * @param removing - the element being removed.
	 */
	protected void onRemoved(TKey removing) {
		// Do nothing
	}
	
	@Override
	public boolean add(TKey element) {
		onInserting(element);
		
		if (replaceMap.containsKey(element)) {
			TKey replacement = replaceMap.get(element);
			onReplacing(element, replacement);
			return delegate().add(replacement);
		} else {
			return delegate().add(element);
		}
	}
	
	@Override
	public void add(int index, TKey element) {
		onInserting(element);
		
		if (replaceMap.containsKey(element)) {
			TKey replacement = replaceMap.get(element);
			onReplacing(element, replacement);
			delegate().add(index, replacement);
		} else {
			delegate().add(index, element);
		}
	}
	
	@Override
	public boolean addAll(Collection<? extends TKey> collection) {
		int oldSize = size();
		
		for (TKey element : collection)
			add(element);
		return size() != oldSize;
	}
	
	@Override
	public boolean addAll(int index, Collection<? extends TKey> elements) {
		int oldSize = size();
		
		for (TKey element : elements)
			add(index++, element);
		return size() != oldSize;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean remove(Object object) {
		boolean success = delegate().remove(object);
	
		if (success)
			onRemoved((TKey) object);
		return success;
	}
	
	@Override
	public TKey remove(int index) {
		TKey removed = delegate().remove(index);
		
		if (removed != null)
			onRemoved(removed);
		return removed;
	}
	
	@Override
	public boolean removeAll(Collection<?> collection) {
		int oldSize = size();
		
		// Use the remove method
		for (Object element : collection)
			remove(element);
		return size() != oldSize;
	}
	
	protected List<TKey> delegate() {
		return underlyingList;
	}

	@Override
	public void clear() {
		delegate().clear();
	}

	@Override
	public boolean contains(Object o) {
		return delegate().contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return delegate().containsAll(c);
	}

	@Override
	public TKey get(int index) {
		return delegate().get(index);
	}

	@Override
	public int indexOf(Object o) {
		return delegate().indexOf(o);
	}

	@Override
	public boolean isEmpty() {
		return delegate().isEmpty();
	}

	@Override
	public Iterator<TKey> iterator() {
		return delegate().iterator();
	}

	@Override
	public int lastIndexOf(Object o) {
		return delegate().lastIndexOf(o);
	}

	@Override
	public ListIterator<TKey> listIterator() {
		return delegate().listIterator();
	}

	@Override
	public ListIterator<TKey> listIterator(int index) {
		return delegate().listIterator(index);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		int oldSize = size();
		
		for (Iterator<TKey> it = delegate().iterator(); it.hasNext(); ) {
			TKey current = it.next();
			
			// Remove elements that are not in the list
			if (!c.contains(current)) {
				it.remove();
				onRemoved(current);
			}
		}
		return size() != oldSize;
	}

	@Override
	public TKey set(int index, TKey element) {
		// Make sure to replace the element
		if (replaceMap.containsKey(element)) {
			TKey replacement = replaceMap.get(element);
			onReplacing(element, replacement);
			return delegate().set(index, replacement);
		} else {
			return delegate().set(index, element);
		}
	}

	@Override
	public int size() {
		return delegate().size();
	}

	@Override
	public List<TKey> subList(int fromIndex, int toIndex) {
		return delegate().subList(fromIndex, toIndex);
	}

	@Override
	public Object[] toArray() {
		return delegate().toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return delegate().toArray(a);
	}
	
	/**
	 * Add a replace rule.
	 * <p>
	 * This automatically replaces every existing element.
	 * @param target - instance to find.
	 * @param replacement - instance to replace with.
	 */
	public synchronized void addMapping(TKey target, TKey replacement) {
		addMapping(target, replacement, false);
	}
	
	/**
	 * Add a replace rule.
	 * <p>
	 * This automatically replaces every existing element.
	 * @param target - instance to find.
	 * @param replacement - instance to replace with.
	 * @param ignoreExisting - whether or not to ignore the existing elements.
	 */
	public synchronized void addMapping(TKey target, TKey replacement, boolean ignoreExisting) {
		replaceMap.put(target, replacement);

		// Replace existing elements
		if (!ignoreExisting) {
			replaceAll(target, replacement);
		}
	}
	
	/**
	 * Revert the given mapping.
	 * @param target - the instance we replaced.
	 */
	public synchronized void removeMapping(TKey target) {
		// Make sure the mapping exist
		if (replaceMap.containsKey(target)) {
			TKey replacement = replaceMap.get(target);
			replaceMap.remove(target);
	
			// Revert existing elements
			replaceAll(replacement, target);
		}
	}
	
	/**
	 * Replace all instances of the given object.
	 * @param find - object to find.
	 * @param replace - object to replace it with.
	 */
	public synchronized void replaceAll(TKey find, TKey replace) {
		for (int i = 0; i < underlyingList.size(); i++) {
			if (Objects.equal(underlyingList.get(i), find)) {
				onReplacing(find, replace);
				underlyingList.set(i, replace);
			}
		}
	}
	
	/**
	 * Undo all replacements.
	 */
	public synchronized void revertAll() {
		
		// No need to do anything else
		if (replaceMap.size() < 1)
			return;
		
		BiMap<TKey, TKey> inverse = replaceMap.inverse();
		
		for (int i = 0; i < underlyingList.size(); i++) {
			TKey replaced = underlyingList.get(i);
			
			if (inverse.containsKey(replaced)) {
				TKey original = inverse.get(replaced);
				onReplacing(replaced, original);
				underlyingList.set(i, original);
			}
		}
		
		replaceMap.clear();
	}
	
	@Override
	protected void finalize() throws Throwable {
		revertAll();
		super.finalize();
	}
}