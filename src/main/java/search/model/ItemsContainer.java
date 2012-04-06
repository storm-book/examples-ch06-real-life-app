package search.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

/**
 * This is a single threaded class intended to store a number of documents that must fit in memory.
 */
public class ItemsContainer {
	Logger log = Logger.getLogger(this.getClass());
	HashMap<String, HashSet<Item>> index;
	HashMap<Item, Item> myItems;
	Set<Item> emptySet= Collections.emptySet();
	
	public ItemsContainer(int initialCapacity) {
		index = new HashMap<String, HashSet<Item>>(initialCapacity, 0.9f);
		myItems= new HashMap<Item, Item>(initialCapacity, 0.9f);
	}
	
	public void add(Item i) {
		if(myItems.containsKey(i))
			update(i);
		
		myItems.put(i, i);
		List<String> words= getItemWords(i);
		
		for (String word : words) {
			HashSet<Item> theSet= index.get(word);
			if(theSet==null) {
				theSet= new HashSet<Item>();
				index.put(word, theSet);
			}
			theSet.add(i);
		}
	}
	

	public void update(Item i) {
		Item myItem= myItems.get(i);
		if(myItem==null)
			add(i);
		else if(!i.title.equals(myItem.title)){
			remove(i);
			add(i);
		}
	}
	
	public void remove(int itemId) {
		Item i= new Item(itemId, "", 0);
		remove(i);
	}
	
	public void remove(Item i) {
		if(myItems.containsKey(i)) {
			Item currentItem= myItems.get(i);
			List<String> words= getItemWords(currentItem);
			for (String word : words) {
				HashSet<Item> theSet= index.get(word);
				if(theSet==null) 
					throw new ConcurrentModificationException("Trying to remove anw item which wasn't indexed, but its on de indexed list!");
				
				if(theSet.size()==1){
					index.remove(word);
				} else {
					theSet.remove(currentItem);
				}
			}
			myItems.remove(currentItem);
			
		}
	}

	private List<String> getItemWords(Item i) {
		ArrayList<String> ret = new ArrayList<String>();
		StringTokenizer strTok = new StringTokenizer(i.title, " ", false);

		while(strTok.hasMoreTokens()) {
			ret.add(strTok.nextToken());
		}
		return ret;
	}
	
	public Set<Item> getItemsContainingWord(String word) {
		Set<Item> items= index.get(word);
		if(items==null){
			return emptySet;
		}
		log.debug("\tWord: ["+word +"] res:"+items.size());
		return items;
	}
	
	public Set<Item> getItemsContainingWords(String words){
		log.debug("Query: ["+words+"]");
		StringTokenizer strTok= new StringTokenizer(words, "-", false);
		HashSet<Item> result= new HashSet<Item>();
		boolean first= true;
		while(strTok.hasMoreTokens()){
			String word= strTok.nextToken();
			if(first){
				first= false;
				result= new HashSet<Item>(getItemsContainingWord(word));
			} else {
				Set<Item> newResults= getItemsContainingWord(word);
				
				for (Iterator<Item> iterator = result.iterator(); iterator.hasNext();) {
					Item item= iterator.next();
					if(!newResults.contains(item)){
						log.debug("\t\tremoving "+item);
						iterator.remove();
					}
				}
			}
		}
		return result;
	}
}