package table;

import table.obstacles.GestionObstacles;
import container.Service;
import utils.*;



public class Table implements Service
{


	public GestionObstacles gestionobstacles;

	// Dépendances
	private Log log;
	private Read_Ini config;
	
	public Table(Log log, Read_Ini config)
	{
		this.log = log;
		this.config = config;
		this.gestionobstacles = new GestionObstacles(log, config);
		initialise();
	}
	
	public void initialise()
	{
	}
	
	//La table
	/**
	 * La table en argument deviendra la copie de this (this reste inchangé)
	 * @param ct
	 */
	public void copy(Table ct) // TODO
	{
        if(!equals(ct))
		{
        	// TODO: faire grande optimisation de ceci a grand coup de hashs
        	
        	
			if(!gestionobstacles.equals(ct.gestionobstacles))
			    gestionobstacles.copy(ct.gestionobstacles);
		}
	}
	
	public Table clone()
	{
		Table cloned_table = new Table(log, config);
		copy(cloned_table);
		return cloned_table;
	}

	/**
	 * Utilisé pour les tests
	 * @param other
	 * @return
	 */
	public boolean equals(Table other)
	{
		return 	false; //TODO
 	}

	@Override
	public void updateConfig() {
		// TODO Auto-generated method stub
		
	}
	

}

