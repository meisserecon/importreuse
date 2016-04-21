package com.meissereconomics.seminar.split;

import java.util.ArrayList;
import java.util.function.BiConsumer;

import com.meissereconomics.seminar.Node;

public class SplitCollection implements BiConsumer<Node, Node> {
	
	private ArrayList<Split> splits;
	
	public SplitCollection(){
		this.splits = new ArrayList<>();
	}
	
	public void accept(Node n1, Node n2){
		this.splits.add(new Split(n1, n2));
	}
	
	public void printAll(){
		System.out.println(Split.getLabel());
		for (Split s: splits){
			System.out.println(s.toString());
		}
	}

}
