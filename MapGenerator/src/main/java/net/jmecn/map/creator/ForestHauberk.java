package net.jmecn.map.creator;

/**
 * 
 * https://github.com/munificent/hauberk/blob/master/lib/src/content/forest.dart
 * 
 * @author yanmaoyuan
 *
 */
public class ForestHauberk extends MapCreator {

	/// A forest is a collection of grassy meadows surrounded by trees and
	/// connected by passages.
	int numMeadows;// >= 10

	/// The number of iterations of Lloyd's algorithm to run on the points.
	///
	/// Fewer results in clumpier, less evenly spaced points. More results in
	/// more evenly spaced but can eventually look too regular.
	int voronoiIterations;// => 5;

	public ForestHauberk(int width, int height) {
		super("creator.forest.hauberk", width, height);
	}

	@Override
	public void initialze() {
		// TODO Auto-generated method stub

	}

	@Override
	public void create() {
		// TODO Auto-generated method stub

	}

}
