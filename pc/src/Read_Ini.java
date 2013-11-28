import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;


public class Read_Ini {
	private String name_local_file = "local.ini";
	private String name_config_file = "config.ini";
	private String path;
	public Properties config = new Properties();
	private Properties local = new Properties();
	
    Enumeration<?> e = local.propertyNames();

	
	public Read_Ini(String path)
	{
		this.path = path;
		try
		{
			this.config.load(new FileInputStream(this.path+this.name_config_file));
		}
		catch  (IOException e)
		{
			e.printStackTrace();
		}
		
		try
		{
			this.config.load(new FileInputStream(this.path+this.name_local_file));
		}
		catch  (IOException e)
		{
			try
			{
				FileOutputStream fileOut = new FileOutputStream(this.path+this.name_local_file);
				this.local.store(fileOut, "Ce fichier est un fichier généré par le programme.\nVous pouvez redéfinir les variables de config.ini dans ce fichier dans un mode de votre choix.\nPS : SopalINT RULEZ !!!\n");
			}
			catch (IOException e2)
			{
				e2.printStackTrace();
			}	
		}	
	}
}
