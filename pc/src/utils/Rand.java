package utils;

import java.util.Random;

/**
 * Classe statique permettant d'obtenir facilement des nombres aléatoires (utilisés pour les pseudo-hash)
 * @author pf
 *
 */

public class Rand {

	private static Random rand = new Random();
	
	public static int getInt()
	{
		return rand.nextInt();
	}
	
}
