package com.radityopw.sqlserverbackuprestore;

import java.util.Scanner;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.util.Properties;
import com.zaxxer.hikari.*;
import java.sql.*;

/**
 * class SqlserverBackupRestoreToolsCli 
 * adalah tools untuk melakukan backup ataupun restore pada RDBMS SQLserver
 *
 */
public class SqlserverBackupRestoreToolsCli 
{
    public static void main( String[] args )
    {
		
		if(args.length > 0)
		{
			
			if(args[0].toLowerCase().equals("config"))
			{
				
				ConfigWizardData cwd = new ConfigWizardData();
				
				config(cwd);
				
			}
			System.exit(0);
		}
		
		showDefaultMessage();
        
    }
	
	public static void showDefaultMessage()
	{
		System.out.println( "Selamat Datang di SqlserverBackupRestoreToolsCLI" );
        System.out.println( "================================================" );
        System.out.println( "cara menggunakan :" );
        System.out.println( "* java com.radityopw.app.SqlserveBackupRestoreToolsCli config : membuat config file" );
        System.out.println( "* java com.radityopw.app.SqlserveBackupRestoreToolsCli backup <configfile> : melakukan backup database berdasarkan config file" );
        System.out.println( "* java com.radityopw.app.SqlserveBackupRestoreToolsCli restore <configfile> : melakukan restore database berdasarkan config file" );
	}
	
	public static void config(ConfigWizardData cwd)
	{
		
		Scanner scanner = new Scanner(System.in);
		
		System.out.println( "Selamat Datang di SqlserverBackupRestoreToolsCLI" );
        System.out.println( "================================================" );
		System.out.println("Modul Config File");
		System.out.println( "================================================" );
		System.out.println("Masukkan lokasi file config dengan absoulte path (ex : c:\\temp\\config1.properties)");
		
		cwd.configFile = scanner.nextLine();
		System.out.println("membuat file di : " + cwd.configFile);
		
		System.out.println("Config Terkait Database");
		System.out.println( "================================================" );
		
		System.out.println("masukkan Database Host");
		cwd.dbHost = scanner.nextLine();
		
		System.out.println("masukkan Database Port");
		cwd.dbPort = scanner.nextLine();
		
		System.out.println("masukkan Database user");
		cwd.dbUser = scanner.nextLine();
		
		System.out.println("masukkan Database Pass");
		cwd.dbPass = scanner.nextLine();
		
		System.out.println("masukkan Database Name");
		cwd.dbName = scanner.nextLine();
		
		System.out.println("masukkan Database Encrypt (true / false)");
		cwd.dbEncrypt = scanner.nextLine();
		
		System.out.println("Config Terkait SSH");
		System.out.println( "================================================" );
		
		System.out.println("masukkan SSH Host");
		cwd.sshHost = scanner.nextLine();
		
		System.out.println("masukkan SSH Port");
		cwd.sshPort = scanner.nextLine();
		
		System.out.println("masukkan SSH User");
		cwd.sshUser = scanner.nextLine();
		
		System.out.println("masukkan SSH Pass");
		cwd.sshPass = scanner.nextLine();
		
		System.out.println("masukkan SSH Folder");
		cwd.sshFolder = scanner.nextLine();
		
		scanner.close();
		
		cwd.save();
		
	}
	
	
}

class ConfigWizardData
{
	
	public String dbHost;
	public String dbPort;
	public String dbUser;
	public String dbPass;
	public String dbName;
	public String dbEncrypt;
	
	public String sshHost;
	public String sshPort;
	public String sshUser;
	public String sshPass;
	public String sshFolder;
	
	public String configFile;
	
	
	public void save()
	{
		
		Properties prop = new Properties();
		OutputStream output = null;
		
		try{

			output = new FileOutputStream(configFile);
			
			prop.setProperty("dbHost",dbHost);
			prop.setProperty("dbPort",dbPort);
			prop.setProperty("dbUser",dbUser);
			prop.setProperty("dbPass",dbPass);
			prop.setProperty("dbName",dbName);
			prop.setProperty("dbEncrypt",dbEncrypt);
			
			
			prop.setProperty("sshHost",sshHost);
			prop.setProperty("sshPort",sshPort);
			prop.setProperty("sshUser",sshUser);
			prop.setProperty("sshPass",sshPass);
			prop.setProperty("sshFolder",sshFolder);
			
			prop.store(output, null);
			
		}catch(Exception e)
		{
			e.printStackTrace();
			
		}finally
		{
			if (output != null) 
			{
                try 
				{
                    output.close();
                } catch (IOException ex) 
				{
                    ex.printStackTrace();
                }
            }
		}
	}
	
	public void load()
	{
		Properties prop = new Properties();
		InputStream output = null;
		
		try
		{

			output = new FileInputStream(configFile);
			
			prop.load(output);
			
			dbHost = prop.getProperty("dbHost");
			dbPort = prop.getProperty("dbPort");
			dbUser = prop.getProperty("dbUser");
			dbPass = prop.getProperty("dbPass");
			dbName = prop.getProperty("dbName");
			dbEncrypt = prop.getProperty("dbEncrypt");
			
			
			sshHost = prop.getProperty("sshHost");
			sshPort = prop.getProperty("sshPort");
			sshUser = prop.getProperty("sshUser");
			sshPass = prop.getProperty("sshPass");
			sshFolder = prop.getProperty("sshFolder");
			
			
			
		}catch(Exception e)
		{
			e.printStackTrace();
			
		}finally
		{
			if (output != null) 
			{
                try 
				{
                    output.close();
                } catch (IOException ex) 
				{
                    ex.printStackTrace();
                }
            }
		}
	}
	
}


class Database
{
	
	private static Database instance;
	
	private HikariDataSource ds;
	
	private Database(ConfigWizardData cwd) throws Exception
	{
		
		Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver"); 
		
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl("jdbc:sqlserver://"+cwd.dbHost+":"+cwd.dbPort+";encrypt="+cwd.dbEncrypt+";databaseName="+cwd.dbName+";");
		config.setUsername(cwd.dbUser);
		config.setPassword(cwd.dbPass);
		config.setMaximumPoolSize(50);
		config.setMinimumIdle(10);
		config.addDataSourceProperty("cachePrepStmts", "true");
		config.addDataSourceProperty("prepStmtCacheSize", "250");
		config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
		
		this.ds = new HikariDataSource(config);
	}
	
	public static Database init(ConfigWizardData cwd) throws Exception
	{
		if ( instance == null )
		{
			instance = new Database(cwd);
			
		}
		
		return instance;
	}
	
	public Connection getConnection() throws Exception
	{
		return this.ds.getConnection();
	}
	
	public void close()
	{
		ds.close();
		
	}
}