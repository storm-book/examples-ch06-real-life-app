package search;

public class RandomItemsPopulator {
	public static String[] possibleWords= new String[] {
		"dvd", "laptop", "usb", "home", "theater", "sound", 
		"notebook", "17", "15", "14", "ipod", "iphone", 
		"mac", "pava", "mate", "bombilla",
		"aire", "acondicionado", "departamento", 
		"casa", "cochera", "silla", "sillon", "mesa", "cable", 
		"cortina", "lavarropa", "lavavajilla", 
		"televisor", "led", "lcd", "ambientes", 
		"cuadro", "decoracion", "pintura", "jarron", "escultura", 
		"ventana", "vidrio", "aluminio", "pvc",
		"nokia", "1100", "blackberry", "curve", 
		"android", "samsung", "galaxy", "sII", "windows", "mobile",
		"aeromodelismo", "automodelismo", "bateria", 
		"motor", "control", "remoto", "alas", "avion", "pilas",
		"combustible", "autos", "peugeot", "206", "207", 
		"307", "308", "407", "408", "fiat", "uno", "palio", 
		"siena", "linea", "stilo", "idea", 
		"chevrolet", "corsa", "agile", "aveo", "vecra", 
		"astra", "cruze", "captiva", "volkswagen", "gol", "trend",
		"power", "fox", "suran", "bora", "vento", "passat", "cc", 
		"tiguan", "touareg", "ford", "fiesta", "ka", "kinetic", 
		"design", "focus", "mondeo", "ecosport", "kuga"
	};
	
	
	public static int MAX_WORDS= 10;
	
	
	public static String getRandomTitle(){
		int cantWords= (int)(Math.random()*(MAX_WORDS-1))+1;
		String res= "";
		for(int i= 0; i< cantWords; i++){
			int ind= (int)(Math.random()*(possibleWords.length-1));
			res+=possibleWords[ind]+" ";
		}
		return res;
	}
}
