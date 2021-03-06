package br.net.fabiozumbi12.redprotect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import org.spongepowered.api.CatalogType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.google.common.reflect.TypeToken;

import br.net.fabiozumbi12.redprotect.config.RPLang;
import br.net.fabiozumbi12.redprotect.hooks.WEListener;

@SuppressWarnings("deprecation")
public class RPUtil {
    static int backup = 0; 
    public static HashMap<Player, HashMap<Location<World>, BlockState>> pBorders = new HashMap<Player, HashMap<Location<World>, BlockState>>();
    private static HashMap<String, UUID> borderIds = new HashMap<String, UUID>();
	public static boolean stopRegen;
        
    public static Text toText(String str){
    	return TextSerializers.FORMATTING_CODE.deserialize(str);
    }
        
    public static boolean isBukkitBlock(BlockState b){
    	RedProtect.logger.debug("default","BlockType: "+b.getType().getName());
    	return b.getType().getName().startsWith("minecraft:");
    }
    
    public static boolean isBukkitEntity(Entity e){
    	RedProtect.logger.debug("default","EntityType: "+e.getType().getName());
    	return Sponge.getGame().getRegistry().getType(EntityType.class, e.getType().getName()).isPresent();
    }
    
    public static ItemType getItemHand(Player p){
    	ItemType itemInHand = ItemTypes.NONE;
        if (p.getItemInHand(HandTypes.MAIN_HAND).isPresent()){
        	itemInHand = p.getItemInHand(HandTypes.MAIN_HAND).get().getItem();
        } else if (p.getItemInHand(HandTypes.OFF_HAND).isPresent()){
        	itemInHand = p.getItemInHand(HandTypes.OFF_HAND).get().getItem();
        }
        return itemInHand;
    }
    
    public static long getNowMillis(){
    	SimpleDateFormat sdf = new SimpleDateFormat(RedProtect.cfgs.getString("region-settings.date-format"));			
		Calendar cal = Calendar.getInstance();
		try {
			cal.setTime(sdf.parse(sdf.format(cal.getTime())));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return cal.getTimeInMillis();
    }
    
    static void saveToZipFile(File file, String ZippedFile, CommentedConfigurationNode conf){
    	try{
    		final ZipOutputStream out = new ZipOutputStream(new FileOutputStream(file));
            ZipEntry e = new ZipEntry(ZippedFile);
            out.putNextEntry(e);

            byte[] data = conf.toString().getBytes();
            out.write(data, 0, data.length);
            out.closeEntry();
            out.close();
    	} catch (Exception e){
    		e.printStackTrace();
    	}    	
    }
    
    public static void removeGuiItem(Player p){
		p.getInventory().slots().forEach(slot -> {
			if (slot.peek().isPresent()){
				ItemStack pitem = slot.peek().get();
				if (removeGuiItem(pitem)){
					slot.poll().get();
				}
			}
		});
	}
    
    public static boolean removeGuiItem(ItemStack item) {    	
    	if (item.get(Keys.ITEM_LORE).isPresent()){
    		try{
    			String lore = item.get(Keys.ITEM_LORE).get().get(1).toPlain();
    			if (RedProtect.cfgs.getDefFlags().contains(lore.replace("§0", "")) || lore.equals(RedProtect.cfgs.getGuiString("separator"))){
    				return true;
    			}
    		} catch (IndexOutOfBoundsException ex){    			
    		}    		
    	}
    	return false;
	}
    
    public static boolean isGuiItem(ItemStack pitem){
    	if (pitem.get(Keys.ITEM_LORE).isPresent() && RedProtect.cfgs.getDefFlags().contains(pitem.get(Keys.ITEM_LORE).get().get(1).toPlain().replace("§0", ""))){
			return true;
		}
		if (pitem.get(Keys.ITEM_LORE).isPresent()){
			List<Text> lore = pitem.get(Keys.ITEM_LORE).get();
			if (RedProtect.cfgs.getGuiSeparator().get(Keys.ITEM_LORE).get().equals(lore)){
				return true;
			}
		}
		return false;
    }
    
    static void SaveToZipSB(File file, String ZippedFile, StringBuilder sb){
    	try{
    		final ZipOutputStream out = new ZipOutputStream(new FileOutputStream(file));
            ZipEntry e = new ZipEntry(ZippedFile);
            out.putNextEntry(e);

            byte[] data = sb.toString().getBytes();
            out.write(data, 0, data.length);
            out.closeEntry();
            out.close();
    	} catch (Exception e){
    		e.printStackTrace();
    	}    	
    }
    
    
    static File genFileName(String Path, Boolean isBackup){
    	File f = new File(Path);
    	if (!f.exists()){
    		f.mkdir();
    	}
    	int count = 1;
		String date = DateNow().replace("/", "-");
    	File logfile = new File(f,date+"-"+count+".zip");
    	File files[] = f.listFiles();
		HashMap<Long, File> keyFiles = new HashMap<Long, File>();
    	if (files.length >= RedProtect.cfgs.getInt("flat-file.max-backups") && isBackup){
    		for (File key:files){
    			keyFiles.put(key.lastModified(), key);
    		}
    		keyFiles.get(Collections.min(keyFiles.keySet())).delete();    		 
    	}
    	
    	while(logfile.exists()){     		
    		count++;
    		logfile = new File(Path+date+"-"+count+".zip");
    	}
    	
    	return logfile;
    }
    
    /**Generate a friendly and unique name for a region based on player name.
     * 
     * @param p Player
     * @param World World
     * @return Name of region
     */
    public static String nameGen(String p, String World){
    	String rname = "";
    	World w = RedProtect.serv.getWorld(World).get();    	
            int i = 0;
            while (true) {
            	int is = String.valueOf(i).length();
                if (p.length() > 13) {
                	rname = p.substring(0, 14-is) + "_" + i;
                }
                else {
                	rname = p + "_" + i;
                }
                if (RedProtect.rm.getRegion(rname, w) == null) {
                    break;
                }
                ++i;
            }           
        return rname;
    }
    
    static String formatName(String name) {
        String s = name.substring(1).toLowerCase();
        String fs = name.substring(0, 1).toUpperCase();
        String ret = String.valueOf(fs) + s;
        ret = ret.replace("_", " ");
        return ret;
    }
    
    static int[] toIntArray(List<Integer> list) {
        int[] ret = new int[list.size()];
        int i = 0;
        for (Integer e : list) {
            ret[i++] = e;
        }
        return ret;
    }
    
    public static String DateNow(){
    	DateFormat df = new SimpleDateFormat(RedProtect.cfgs.getString("region-settings.date-format"));
        Date today = Calendar.getInstance().getTime(); 
        String now = df.format(today);
		return now;    	
    }
    
    static String HourNow(){
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int min = Calendar.getInstance().get(Calendar.MINUTE);
        int sec = Calendar.getInstance().get(Calendar.SECOND);        
		return "["+hour+":"+min+":"+sec+"]";    	
    }
    
    static void fixWorld(String regionname){
    	for (World w:RedProtect.serv.getWorlds()){
    		Region r = RedProtect.rm.getRegion(regionname, w);
    		if (r != null){
    			r.setWorld(w.getName());
    		}
    	}
    }
    
    //TODO read all db
    static void ReadAllDB(Set<Region> regions){     	
    	RedProtect.logger.info("Loaded " + regions.size() + " regions (" + RedProtect.cfgs.getString("file-type") + ")");
    	int i = 0;
    	int pls = 0;
    	int origupdt = 0;
    	int namesupdt = 0;
    	int purged = 0;
    	int sell = 0;
    	int dateint = 0;
    	int cfm = 0;
    	int delay = 0;
    	int skipped = 0;
    	Date now = null;   	 
    	SimpleDateFormat dateformat = new SimpleDateFormat(RedProtect.cfgs.getString("region-settings.date-format"));

    	boolean checkNames = RedProtect.cfgs.getBool("hooks.check-uuid-names-onstart");        	
    	
		try {
			now = dateformat.parse(DateNow());
		} catch (ParseException e1) {
			RedProtect.logger.severe("The 'date-format' don't match with date 'now'!!");
		}
		
        for (Region r:regions){
        	boolean serverRegion = false;
        	
        	if (r.isLeader(RedProtect.cfgs.getString("region-settings.default-leader"))){
        		serverRegion = true;
        		r.setDate(DateNow());
        	} 
        	
        	//purge regions
        	if (RedProtect.cfgs.getBool("purge.enabled") && !serverRegion){
        		Date regiondate = null;
            	try {
    				regiondate = dateformat.parse(r.getDate());
    			} catch (ParseException e) {
    				RedProtect.logger.severe("The 'date-format' don't match with region date!!");
    				e.printStackTrace();
    			}
            	Long days = TimeUnit.DAYS.convert(now.getTime() - regiondate.getTime(), TimeUnit.MILLISECONDS);
            	
            	for (String play:RedProtect.cfgs.getStringList("purge.ignore-regions-from-players")){
            		if (r.isLeader(RPUtil.PlayerToUUID(play)) || r.isAdmin(RPUtil.PlayerToUUID(play))){
            			continue;
            		}
    			}           	
            	
            	if (days > RedProtect.cfgs.getInt("purge.remove-oldest")){ 
            		RedProtect.rm.remove(r, RedProtect.serv.getWorld(r.getWorld()).get());
        			//r.delete();
        			purged++;
        			RedProtect.logger.warning("Purging " + r.getName() + " - Days: " + days);
        			
            		if (RedProtect.WE && RedProtect.cfgs.getBool("purge.regen.enable")){
            			if (r.getArea() <= RedProtect.cfgs.getInt("purge.regen.max-area-regen")){
            				WEListener.regenRegion(r, Sponge.getServer().getWorld(r.getWorld()).get(), r.getMaxLocation(), r.getMinLocation(), delay, null, true);                		
                    		delay=delay+10;
            			} else {
            				skipped++;
            				continue;
            			}             			
            		} else {
            			RedProtect.rm.remove(r, RedProtect.serv.getWorld(r.getWorld()).get());
            			//r.delete();
            			purged++;
            			RedProtect.logger.warning("Purging " + r.getName() + " - Days: " + days);
            		}
            		continue;
            	}
        	}    
        	
        	
        	//sell rergions
        	if (RedProtect.cfgs.getBool("sell.enabled") && !serverRegion){
        		Date regiondate = null;
            	try {
    				regiondate = dateformat.parse(r.getDate());
    			} catch (ParseException e) {
    				RedProtect.logger.severe("The 'date-format' don't match with region date!!");
    				e.printStackTrace();
    			}
            	Long days = TimeUnit.DAYS.convert(now.getTime() - regiondate.getTime(), TimeUnit.MILLISECONDS);
            	
            	List<String> players = new ArrayList<String>();
            	for (String play:RedProtect.cfgs.getStringList("sell.ignore-regions-from-players")){
            		players.add(RPUtil.PlayerToUUID(play));
    			}           	
            	
            	if (days > RedProtect.cfgs.getInt("sell.sell-oldest")){        
                	RedProtect.logger.warning("Selling " + r.getName() + " - Days: " + days);
            		RPEconomy.putToSell(r, RedProtect.cfgs.getString("region-settings.default-leader"), RPEconomy.getRegionValue(r));
            		sell++;
            		RedProtect.rm.saveAll();
            		continue;
            	}
        	}
        	
        	//Update player names
        	List<String> leadersl = r.getLeaders();
        	List<String> adminsl = r.getAdmins();
        	List<String> membersl = r.getMembers();  
    		
        	if (origupdt >= 90 || namesupdt >= 90){
        		try {
					Thread.sleep(100L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
        	}
        	
        	if (!serverRegion && checkNames){
        		if (RedProtect.OnlineMode){
                	for (int l = 0; l < leadersl.size(); l++){
                		String pname = leadersl.get(l).replace("[", "").replace("]", "");
                		if (!isUUIDs(pname) && pname != null && !RPUtil.isDefaultServer(pname)){
                			String uuid = MojangUUIDs.getUUID(pname);
                			if (uuid == null){
                				uuid = PlayerToUUID(pname);
                			}
                    		RedProtect.logger.warning("Leader from: " + pname);
                    		leadersl.set(l, uuid);
                    		RedProtect.logger.warning("To UUID: " + uuid);
                    		origupdt++;
                		}             		
                	} 
                	for (int o = 0; o < adminsl.size(); o++){
                		String pname = adminsl.get(o).replace("[", "").replace("]", "");
                		if (!isUUIDs(pname) && pname != null && !RPUtil.isDefaultServer(pname)){
                			String uuid = MojangUUIDs.getUUID(pname);
                			if (uuid == null){
                				uuid = PlayerToUUID(pname);
                			}
                    		RedProtect.logger.warning("Admin from: " + pname);
                    		adminsl.set(o, uuid);
                    		RedProtect.logger.warning("To UUID: " + uuid);
                    		origupdt++;
                		}             		
                	}        	
                	for (int m = 0; m < membersl.size(); m++){
                		String pname = membersl.get(m).replace("[", "").replace("]", "");     		
                		if (!isUUIDs(pname) && pname != null && !RPUtil.isDefaultServer(pname)){
                			String uuid = MojangUUIDs.getUUID(pname);
                			if (uuid == null){
                				uuid = PlayerToUUID(pname);
                			}
                    		RedProtect.logger.warning("Member from: " + pname);
                    		membersl.set(m, uuid);
                    		RedProtect.logger.warning("To UUID: " + uuid);  
                    		origupdt++;
                		}              		
                	}
                	r.setLeaders(leadersl);
                	r.setAdmins(adminsl);
                	r.setMembers(membersl);            	
                	if (origupdt > 0){
                		pls++;
                	}            	
            	} 
            	//if Offline Mode
            	else {
            		for (int l = 0; l < leadersl.size(); l++){
            			if (isUUIDs(leadersl.get(l)) && !RPUtil.isDefaultServer(leadersl.get(l))){
            				try {
    							String name = MojangUUIDs.getName(leadersl.get(l));
    							if (name == null){
    								name = UUIDtoPlayer(leadersl.get(l));
    							}
    							RedProtect.logger.warning("Leader from: " + leadersl.get(l));
    							leadersl.set(l, name.toLowerCase());
    							RedProtect.logger.warning("To UUID: " + name); 
    							namesupdt++;
    						} catch (Exception e) {
    							e.printStackTrace();
    						}
            			}
            		}
            		
            		for (int a = 0; a < adminsl.size(); a++){
            			if (isUUIDs(adminsl.get(a)) && !RPUtil.isDefaultServer(adminsl.get(a))){
            				try {
    							String name = MojangUUIDs.getName(adminsl.get(a));
    							if (name == null){
    								name = UUIDtoPlayer(adminsl.get(a));
    							}
    							RedProtect.logger.warning("Admin from: " + adminsl.get(a));
    							adminsl.set(a, name.toLowerCase());
    							RedProtect.logger.warning("To UUID: " + name); 
    							namesupdt++;
    						} catch (Exception e) {
    							e.printStackTrace();
    						}
            			}
            		}
            		
            		for (int m = 0; m < membersl.size(); m++){
            			if (isUUIDs(membersl.get(m)) && !RPUtil.isDefaultServer(membersl.get(m))){
            				try {
    							String name = MojangUUIDs.getName(membersl.get(m));
    							if (name == null){
    								name = UUIDtoPlayer(membersl.get(m));
    							}
    							RedProtect.logger.warning("Member from: " + membersl.get(m));
    							membersl.set(m, name.toLowerCase());
    							RedProtect.logger.warning("To UUID: " + name); 
    							namesupdt++;
    						} catch (Exception e) {
    							e.printStackTrace();
    						}
            			}
            		}
            		r.setLeaders(leadersl);
                	r.setAdmins(adminsl);
                	r.setMembers(membersl);
                	if (namesupdt > 0){
                		pls++;
                	} 
            	}
        	}
        	     	
        	if (pls > 0){
        		RedProtect.logger.sucess("["+pls+"]Region updated &6&l" + r.getName() + "&a&l. Leaders &6&l" + r.getLeadersDesc());
            }       
        	
        	//conform region names        	
        	if (r.getName().contains("/")){
    			String rname = r.getName().replace("/", "|");
    			RedProtect.rm.renameRegion(rname, r);
    			cfm++;
    		}   
        }     
        
        if (delay > 0){
    		RedProtect.logger.warning("&c> There's "+delay/10+" regions to be regenerated at 2 regions/second.");
    		RedProtect.logger.severe("&cRegen can take long time, but your players can join and play normally!");
        }
        
        if (cfm > 0){
    		RedProtect.logger.sucess("["+cfm+"] Region names conformed!");
        }
        
        if (dateint > 0){
			RedProtect.logger.info("Updated "+ dateint +" last visit users!");
			RedProtect.rm.saveAll();
    	}
                   	        
        if (i > 0 || pls > 0){
        	if (i > pls){
            	RedProtect.logger.sucess("Updated a total of &6&l" + (i-pls) + "&a&l regions!");
        	} else {
            	RedProtect.logger.sucess("Updated a total of &6&l" + (pls-i) + "&a&l regions!");
        	}
        	RedProtect.rm.saveAll();        	
        	RedProtect.logger.sucess("Regions saved!");  
        	pls = 0;
        	i = 0;
        }
        
        if (skipped > 0){
        	RedProtect.logger.sucess(skipped + " regions skipped due to max size limit to regen!");
        	skipped = 0;
        }
        
        if (purged > 0){
        	RedProtect.logger.warning("Purged a total of &6&l" + purged + "&a&l regions!");
        	purged = 0;
        }
        
        if (sell > 0){
        	RedProtect.logger.warning("Put to sell a total of &6&l" + sell + "&a&l regions!");
        	sell = 0;
        }
        regions.clear();   
	}
      
    
    private static boolean isDefaultServer(String check) {
    	return check.equalsIgnoreCase(RedProtect.cfgs.getString("region-settings.default-leader"));
	}

	public static String PlayerToUUID(String PlayerName){
    	if (PlayerName == null || PlayerName.equals("")){
    		return null;
    	}
    	
    	//check if is already UUID
    	if (isUUIDs(PlayerName) || isDefaultServer(PlayerName) || (PlayerName.startsWith("[") && PlayerName.endsWith("]"))){
    		return PlayerName;
    	}
    	
    	String uuid = PlayerName;

    	if (!RedProtect.OnlineMode){
    		uuid = uuid.toLowerCase();
    		return uuid;
    	}
    	
    	UserStorageService uss = Sponge.getGame().getServiceManager().provide(UserStorageService.class).get();
    	
    	Optional<GameProfile> ogpName = uss.getAll().stream().filter(f -> f.getName().isPresent() && f.getName().get().equalsIgnoreCase(PlayerName)).findFirst();
		if (ogpName.isPresent()){
			return ogpName.get().getUniqueId().toString();
		} else {
			Optional<Player> p = RedProtect.serv.getPlayer(PlayerName);
			if (p.isPresent()){
				return p.get().getUniqueId().toString();
			}
		}
		
		return uuid;    	
    }
    
	public static String UUIDtoPlayer(String uuid){
    	if (uuid == null){
    		return null;
    	}
    	
    	//check if is UUID
    	if (isDefaultServer(uuid) || !isUUIDs(uuid)){
    		return uuid;
    	}
    	
    	String PlayerName = "UnknowPlayer";
    	UUID uuids = UUID.fromString(uuid);
    	
    	if (!RedProtect.OnlineMode){	    	
    		return uuid.toLowerCase();
    	}    	
    	
    	UserStorageService uss = Sponge.getGame().getServiceManager().provide(UserStorageService.class).get();
    	/*
		Optional<GameProfile> ogpName = uss.getAll().stream().filter(f -> f.getUniqueId().equals(uuids)).findFirst();
		if (ogpName.isPresent()){
			return ogpName.get().getName().get();
		}
		*/
		if (uss.get(uuids).isPresent()){
			return uss.get(uuids).get().getName();
		}
		    	
		return PlayerName;    	
    }
    
	public static User getUser(String name){
		UserStorageService uss = Sponge.getGame().getServiceManager().provide(UserStorageService.class).get();
		if (isUUIDs(name)){
			UUID uuid = UUID.fromString(name);
			if (uss.get(uuid).isPresent()){
				return uss.get(uuid).get();
			}
		} else {
			if (uss.get(name).isPresent()){
				return uss.get(name).get();
			}
		}		
		return null;
	}
	
	private static boolean isUUIDs(String uuid){
    	if (uuid == null){
    		return false;
    	}
    	try{
    		UUID.fromString(uuid);
    		return true;
    	} catch (IllegalArgumentException e){
    		return false;
    	}		
    }
    
    static void addRegion(List<Region> regions, World w){    	
    	for (int i = 0; i < regions.size(); i++){
    		if (!RedProtect.rm.getRegionsByWorld(w).contains(regions.get(i))){
    			RedProtect.logger.warning("["+(i+1)+"/"+regions.size()+"]Adding regions to database! This may take some time...");
        		RedProtect.rm.add(regions.get(i), w);       		                		
    		}
		}	 
    	regions.clear();
    }
    
    public static Object parseObject(String value){
    	Object obj = value;
    	try {
    		obj = Integer.parseInt(value);
    	} catch(NumberFormatException e){
    		if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")){
    			obj = Boolean.parseBoolean(value);
        	}
    	}
    	return obj;
    }
    
    public static boolean mysqlToFile(){    	
    	HashMap<String,Region> regions = new HashMap<String, Region>();
    	int saved = 1;
    	
        try {
        	Connection dbcon = DriverManager.getConnection("jdbc:mysql://"+RedProtect.cfgs.getString("mysql.host")+"/"+RedProtect.cfgs.getString("mysql.db-name")+"?autoReconnect=true", RedProtect.cfgs.getString("mysql.user-name"), RedProtect.cfgs.getString("mysql.user-pass"));
            	
        	for (World world:Sponge.getServer().getWorlds()){
            	String tableName = RedProtect.cfgs.getString("mysql.table-prefix")+world.getName();
                PreparedStatement st = dbcon.prepareStatement("SELECT * FROM `"+tableName+"` WHERE world=?");
				st.setString(1, world.getName());
                ResultSet rs = st.executeQuery();            
                while (rs.next()){ 
                	List<String> leaders = new ArrayList<String>();
                	List<String> admins = new ArrayList<String>();
                    List<String> members = new ArrayList<String>();
                    HashMap<String, Object> flags = new HashMap<String, Object>();                      
                    
                    int maxMbrX = rs.getInt("maxMbrX");
                    int minMbrX = rs.getInt("minMbrX");
                    int maxMbrZ = rs.getInt("maxMbrZ");
                    int minMbrZ = rs.getInt("minMbrZ");
                    int maxY = rs.getInt("maxY");
                    int minY = rs.getInt("minY");
                    int prior = rs.getInt("prior");
                    String rname = rs.getString("name");
                    String date = rs.getString("date");
                    String wel = rs.getString("wel");
                    String rent = rs.getString("rent");
                    long value = rs.getLong("value");
                    
                    Location<World> tppoint = null;
                    if (rs.getString("tppoint") != null && !rs.getString("tppoint").equalsIgnoreCase("")){
                    	String tpstring[] = rs.getString("tppoint").split(",");
                        tppoint = new Location<World>(world, Double.parseDouble(tpstring[0]), Double.parseDouble(tpstring[1]), Double.parseDouble(tpstring[2]))/*, 
                        		Float.parseFloat(tpstring[3]), Float.parseFloat(tpstring[4]))*/;
                    }                    
                                        
                    for (String member:rs.getString("members").split(", ")){
                    	if (member.length() > 0){
                    		members.add(member);
                    	}                	
                    }
                    for (String admin:rs.getString("admins").split(", ")){
                    	if (admin.length() > 0){
                    		admins.add(admin);
                    	}                	
                    }
                    for (String leader:rs.getString("leaders").split(", ")){
                    	if (leader.length() > 0){
                    		leaders.add(leader);
                    	}                	
                    }
                    for (String flag:rs.getString("flags").split(",")){
                    	String key = flag.split(":")[0];
                    	String replace = new String(key+":");
                    	if (replace.length() <= flag.length()){
                    		flags.put(key, RPUtil.parseObject(flag.substring(replace.length())));  
                    	} 
                    }      
                    Region newr = new Region(rname, admins, members, leaders, maxMbrX, minMbrX, maxMbrZ, minMbrZ, minY, maxY, flags, wel, prior, world.getName(), date, value, tppoint, true);
                    if (rent.split(":").length >= 3){
              	    	newr.setRentString(rent);
              		}
                    regions.put(rname, newr);
                } 
                st.close(); 
                rs.close();  
                
                File datf  = new File(RedProtect.configDir+"data", "data_" + world.getName() + ".conf");
				ConfigurationLoader<CommentedConfigurationNode> regionManager = HoconConfigurationLoader.builder().setPath(datf.toPath()).build();
				CommentedConfigurationNode fileDB = regionManager.createEmptyNode();
				
				for (Region r:regions.values()){
					if (r.getName() == null){
						continue;
					}
					
					if (RedProtect.cfgs.getBool("flat-file.region-per-file")) {
						if (!r.toSave()){
							continue;
						}
						datf  = new File(RedProtect.configDir+"data", world.getName()+File.separator+ r.getName() + ".conf"); 
						regionManager = HoconConfigurationLoader.builder().setPath(datf.toPath()).build();
						fileDB = regionManager.createEmptyNode();
					}
					
					fileDB = RPUtil.addProps(fileDB, r); 
					saved++;
					
					if (RedProtect.cfgs.getBool("flat-file.region-per-file")) { 
						saveConf(fileDB, regionManager);
						r.setToSave(false);						  				
					}     			
				}	 
				
				if (!RedProtect.cfgs.getBool("flat-file.region-per-file")) { 
					backupRegions(fileDB, world.getName());	
					saveConf(fileDB, regionManager);									
				} else {
					//remove deleted regions
					File wfolder = new File(RedProtect.configDir+"data", world.getName());
					if (wfolder.exists()){
						File[] listOfFiles = wfolder.listFiles();    				
			    		for (File region:listOfFiles){
			    			if (region.isFile() && !regions.containsKey(region.getName().replace(".conf", ""))){
			    				region.delete();
			    			}
			    		}
					}
				}
            }  
        	dbcon.close();
        	
        	if (saved > 0){
    			RedProtect.logger.sucess((saved-1) + " regions converted to File with sucess!");
    		}        	
    	} catch (SQLException e) {
            e.printStackTrace();
        }
        
    	return true;
    }
    
    public static boolean fileToMysql() throws Exception{
		if (!RedProtect.cfgs.getString("file-type").equalsIgnoreCase("file")){
			return false;
		}
		RedProtect.rm.saveAll();
		
		initMysql();//Create tables
		int counter = 1;
		
		for (World world:Sponge.getServer().getWorlds()){
			
			String dbname = RedProtect.cfgs.getString("mysql.db-name");
		    String url = "jdbc:mysql://"+RedProtect.cfgs.getString("mysql.host")+"/";
		    String tableName = RedProtect.cfgs.getString("mysql.table-prefix")+world.getName();
		    
			Connection dbcon = DriverManager.getConnection(url + dbname, RedProtect.cfgs.getString("mysql.user-name"), RedProtect.cfgs.getString("mysql.user-pass"));
			
			for (Region r:RedProtect.rm.getRegionsByWorld(world)){
				if (!regionExists(dbcon, r.getName(), tableName)) {
					try {                
		                PreparedStatement st = dbcon.prepareStatement("INSERT INTO `"+tableName+"` (name,leaders,admins,members,maxMbrX,minMbrX,maxMbrZ,minMbrZ,minY,maxY,centerX,centerZ,date,wel,prior,world,value,tppoint,rent,candelete,flags) "
		                		+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");    
		                st.setString(1, r.getName());
		                st.setString(2, r.getLeaders().toString().replace("[", "").replace("]", ""));
		                st.setString(3, r.getAdmins().toString().replace("[", "").replace("]", ""));
		                st.setString(4, r.getMembers().toString().replace("[", "").replace("]", ""));
		                st.setInt(5, r.getMaxMbrX());
		                st.setInt(6, r.getMinMbrX());
		                st.setInt(7, r.getMaxMbrZ());
		                st.setInt(8, r.getMinMbrZ());
		                st.setInt(9, r.getMinY());
		                st.setInt(10, r.getMaxY());
		                st.setInt(11, r.getCenterX());
		                st.setInt(12, r.getCenterZ());
		                st.setString(13, r.getDate());
		                st.setString(14, r.getWelcome());
		                st.setInt(15, r.getPrior());
		                st.setString(16, r.getWorld());
		                st.setLong(17, r.getValue());
		                st.setString(18, r.getTPPointString());
		                st.setString(19, r.getRentString());
		                st.setInt(20, r.canDelete() ? 1 : 0);
		                st.setString(21, r.getFlagStrings());
		                
		                st.executeUpdate();
		                st.close();
		                counter++;
		            }
		            catch (SQLException e) {
		                e.printStackTrace();
		            }
		        } else {
		        	//if exists jump
		        	continue;
		        }
			}
			dbcon.close();
		}		
		if (counter > 0){
			RedProtect.logger.sucess((counter-1) + " regions converted to Mysql with sucess!");
		}
		return true;		
	}
	
    private static void initMysql() throws Exception{
		for (World world:Sponge.getServer().getWorlds()){
			
		    String url = "jdbc:mysql://"+RedProtect.cfgs.getString("mysql.host")+"/";
		    String reconnect = "?autoReconnect=true";
		    String tableName = RedProtect.cfgs.getString("mysql.table-prefix")+world.getName();
		    
	        try {
	            Class.forName("com.mysql.jdbc.Driver");
	        }
	        catch (ClassNotFoundException e2) {
	            RedProtect.logger.severe("Couldn't find the driver for MySQL! com.mysql.jdbc.Driver.");
	            return;
	        }
	        PreparedStatement st = null;	        
	        try {
	        	if (!checkTableExists(tableName)) {
	        		//create db
	                Connection con = DriverManager.getConnection(url+RedProtect.cfgs.getString("mysql.db-name")+reconnect, RedProtect.cfgs.getString("mysql.user-name"), RedProtect.cfgs.getString("mysql.user-pass"));  
	                st = con.prepareStatement("CREATE TABLE `"+tableName+"` "
	                		+ "(name varchar(20) PRIMARY KEY NOT NULL, leaders varchar(36), admins longtext, members longtext, maxMbrX int, minMbrX int, maxMbrZ int, minMbrZ int, centerX int, centerZ int, minY int, maxY int, date varchar(10), wel longtext, prior int, world varchar(100), value Long not null, tppoint mediumtext, rent longtext, flags longtext, candelete tinyint(1)) CHARACTER SET utf8 COLLATE utf8_general_ci");
	                st.executeUpdate();
	                st.close();
	                st = null;
	                RedProtect.logger.info("Created table: "+tableName+"!");  
	            }
				addNewColumns(tableName);
	        }
	        catch (SQLException e) {
	            e.printStackTrace();
	            RedProtect.logger.severe("There was an error while parsing SQL, redProtect will still with actual DB setting until you change the connection options or check if a Mysql service is running. Use /rp reload to try again");
	        }
	        finally {
	            if (st != null) {
	                st.close();
	            }
	        }
		}	    
	}
		
	private static void addNewColumns(String tableName){
		try {
			String url = "jdbc:mysql://"+RedProtect.cfgs.getString("mysql.host")+"/";
			Connection con = DriverManager.getConnection(url + RedProtect.cfgs.getString("mysql.db-name"), RedProtect.cfgs.getString("mysql.user-name"), RedProtect.cfgs.getString("mysql.user-pass"));
			DatabaseMetaData md = con.getMetaData();
			ResultSet rs = md.getColumns(null, null, tableName, "candelete");
			if (!rs.next()) {				
				PreparedStatement st = con.prepareStatement("ALTER TABLE `"+tableName+"` ADD `candelete` tinyint(1) NOT NULL default '1'");
				st.executeUpdate();
			}
			rs.close();
			rs = md.getColumns(null, null, tableName, "rent");
			if (!rs.next()) {				
				PreparedStatement st = con.prepareStatement("ALTER TABLE `"+tableName+"` ADD `rent` longtext");
				st.executeUpdate();
			}
			rs.close();
			con.close();			
		} catch (SQLException e) {
			e.printStackTrace();
		}		
	}
	
    public static void backupRegions(CommentedConfigurationNode fileDB, String world) {
        if (!RedProtect.cfgs.getBool("flat-file.backup")) {
            return;
        }
        
        File bfolder = new File(RedProtect.configDir+"backups"+File.separator);
        if (!bfolder.exists()){
        	bfolder.mkdir();
        }
        
        File folder = new File(RedProtect.configDir+"backups"+File.separator+world+File.separator);
        if (!folder.exists()){
        	folder.mkdir();
        	RedProtect.logger.info("Created folder: " + folder.getPath()); 
        }
        
        //Save backup
        if (RPUtil.genFileName(folder.getPath()+File.separator, true) != null){
        	RPUtil.saveToZipFile(RPUtil.genFileName(folder.getPath()+File.separator, true), "data_" + world + ".conf", fileDB); 
        }
		       
    }
    
	private static boolean regionExists(Connection dbcon, String name, String tableName) {
        int total = 0;
        try {
        	PreparedStatement st = dbcon.prepareStatement("SELECT COUNT(*) FROM `"+tableName+"` WHERE name = ?");
        	st.setString(1, name);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                total = rs.getInt("COUNT(*)");
            }
            st.close();
            rs.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return total > 0;
    }	
	
	private static boolean checkTableExists(String tableName) throws SQLException {
        try {
        	RedProtect.logger.debug("default", "Checking if table exists... " + tableName);
        	Connection con = DriverManager.getConnection("jdbc:mysql://"+RedProtect.cfgs.getString("mysql.host")+"/"+RedProtect.cfgs.getString("mysql.db-name"),RedProtect.cfgs.getString("mysql.user-name"), RedProtect.cfgs.getString("mysql.user-pass"));
        	DatabaseMetaData meta = con.getMetaData();
            ResultSet rs = meta.getTables(null, null, tableName, null);
            if (rs.next()) {
            	con.close();
            	rs.close();
            	return true;               
            }    
            con.close();
        	rs.close();
        } catch (SQLException e){
        	e.printStackTrace();
        }        
        return false;
    }
	
	public static void startFlagChanger(final String r, final String flag, final Player p){
		RedProtect.changeWait.add(r+flag);
		Sponge.getScheduler().createSyncExecutor(RedProtect.plugin).schedule(new Runnable() { 
			public void run() {
				if (RedProtect.changeWait.contains(r+flag)){
					/*if (p != null && p.isOnline()){
						RPLang.sendMessage(p, RPLang.get("gui.needwait.ready").replace("{flag}", flag));
					}*/
					RedProtect.changeWait.remove(r+flag);				
				} 
			}
			}, RedProtect.cfgs.getInt("flags-configuration.change-flag-delay.seconds"), TimeUnit.SECONDS);
	}
	
	public static int getUpdatedPrior(Region region) {
		int regionarea = region.getArea();  
		int prior = region.getPrior();
        Region topRegion = RedProtect.rm.getTopRegion(RedProtect.serv.getWorld(region.getWorld()).get(), region.getCenterX(), region.getCenterY(), region.getCenterZ());
        Region lowRegion = RedProtect.rm.getLowRegion(RedProtect.serv.getWorld(region.getWorld()).get(), region.getCenterX(), region.getCenterY(), region.getCenterZ());
        
        if ((topRegion != null && topRegion.getID().equals(region.getID())) || (lowRegion != null && lowRegion.getID().equals(region.getID()))){
        	return prior;
        }
        
        if (lowRegion != null){
        	if (regionarea > lowRegion.getArea()){
        		prior = lowRegion.getPrior() - 1;
        	} else if (regionarea < lowRegion.getArea() && regionarea < topRegion.getArea() ){
        		prior = topRegion.getPrior() + 1;
        	} else if (regionarea < topRegion.getArea()){
        		prior = topRegion.getPrior() + 1;
        	} 
        }
		return prior;
	}
	
	
	/** Show the border of region for defined seconds.
	 * @param p
	 * @param loc1
	 * @param loc2
	 */
	public static void addBorder(final Player p, List<Location<World>> locs) {		
		final World w = p.getWorld();
		boolean msg = true;
		
		if (pBorders.containsKey(p)){
			for (Location<World> loc:pBorders.get(p).keySet()){     
				loc.setBlock(pBorders.get(p).get(loc), Cause.of(NamedCause.owner(RedProtect.plugin)));   
    		}
			if (borderIds.containsKey(p.getName())){
				Sponge.getScheduler().getTaskById(borderIds.get(p.getName())).get().cancel();
				borderIds.remove(p.getName());
			}
			pBorders.remove(p);
			msg = false;
		}
		
		final HashMap<Location<World>, BlockState> borderBlocks = new HashMap<Location<World>, BlockState>();				
		
		for (Location<World> loc:locs){
			loc = new Location<World>(w, loc.getBlockX(), p.getLocation().getBlockY(), loc.getBlockZ());
			BlockState b = w.getBlock(loc.getBlockPosition());
        	if (b.getType().equals(BlockTypes.AIR) || b.getType().equals(BlockTypes.WATER)){
        		borderBlocks.put(loc, b);
        		w.setBlockType(loc.getBlockPosition(), RedProtect.cfgs.getMaterial("region-settings.border.material"), Cause.of(NamedCause.owner(RedProtect.plugin)));
        	} 
		}
		
		if (borderBlocks.isEmpty()){
			RPLang.sendMessage(p, "cmdmanager.bordernospace");
		} else {
			if (msg){
				RPLang.sendMessage(p, "cmdmanager.addingborder");
			}			
			pBorders.put(p, borderBlocks);
			UUID taskid = Sponge.getScheduler().createSyncExecutor(RedProtect.plugin).schedule(new Runnable(){
				@Override
				public void run() {
					if (pBorders.containsKey(p)){
	            		for (Location<World> loc:pBorders.get(p).keySet()){
	            			loc.setBlock(pBorders.get(p).get(loc), Cause.of(NamedCause.owner(RedProtect.plugin)));            			
	            		}
	            		if (borderIds.containsKey(p.getName())){
	            			borderIds.remove(p.getName());
	            		}
	            		pBorders.remove(p);
	            		RPLang.sendMessage(p, "cmdmanager.removingborder");
					}
				}    		
	    	}, RedProtect.cfgs.getInt("region-settings.border.time-showing"), TimeUnit.SECONDS).getTask().getUniqueId(); 
			borderIds.put(p.getName(), taskid);
		}		
    }		
	
	public static String StripName(String pRName) {
        String regionName;
		if (pRName.length() > 13) {
            regionName = pRName.substring(0, 13);
        } else {
        	regionName = pRName;
        } 
		return regionName;
	}
	
	public static <T extends CatalogType> boolean testRegistry(Class<T> class1, String value){
		return (Sponge.getRegistry().getType(class1, value).isPresent());
	}
	
	public static <T extends CatalogType> Object getRegistryFor(Class<T> class1, String value){
		if (!testRegistry(class1,value)){
			return null;
		}
		return Sponge.getRegistry().getType(class1, value).get();
	}
	
	public static int simuleTotalRegionSize(String player, Region r2) {
		int total = 0;
		int regs = 0;			
		for (Location<World> loc:r2.get4Points(r2.getCenterY())){		
			Map<Integer, Region> pregs = RedProtect.rm.getGroupRegion(loc.getExtent(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
			pregs.remove(r2.getPrior());				
			Region other = null;
			if (pregs.size() > 0){
				other = pregs.get(Collections.max(pregs.keySet()));
			} else {
				continue;
			}				
			//RedProtect.logger.severe("Reg: "+other.getName());
			if (!r2.getID().equals(other.getID()) && r2.getPrior() > other.getPrior() && other.isLeader(player)){
				regs++;
				//RedProtect.logger.severe("Reg added: "+other.getName());
			}
		}			
		//RedProtect.logger.severe("Regs size: "+regs);
		if (regs == 0 || regs != 4){
			total += r2.getArea();
		} 
		return total;
	}
	
	public static CommentedConfigurationNode addProps(CommentedConfigurationNode fileDB, Region r){
		String rname = r.getName().replace(".", "-");
		fileDB.getNode(rname,"name").setValue(rname);
		fileDB.getNode(rname,"lastvisit").setValue(r.getDate());
		fileDB.getNode(rname,"leaders").setValue(r.getLeaders());
		fileDB.getNode(rname,"admins").setValue(r.getAdmins());
		fileDB.getNode(rname,"members").setValue(r.getMembers());
		fileDB.getNode(rname,"priority").setValue(r.getPrior());
		fileDB.getNode(rname,"welcome").setValue(r.getWelcome());
		fileDB.getNode(rname,"world").setValue(r.getWorld());
		fileDB.getNode(rname,"maxX").setValue(r.getMaxMbrX());
		fileDB.getNode(rname,"maxZ").setValue(r.getMaxMbrZ());
		fileDB.getNode(rname,"minX").setValue(r.getMinMbrX());
		fileDB.getNode(rname,"minZ").setValue(r.getMinMbrZ());	
		fileDB.getNode(rname,"maxY").setValue(r.getMaxY());
		fileDB.getNode(rname,"minY").setValue(r.getMinY());    
		fileDB.getNode(rname,"rent").setValue(r.getRentString());   
		fileDB.getNode(rname,"candelete").setValue(r.canDelete());
		for (String flag:r.flags.keySet()){
			fileDB.getNode(rname,"flags",flag).setValue(r.flags.get(flag));	
		}
		fileDB.getNode(rname,"value").setValue(r.getValue());
		
		Location<World> loc = r.getTPPoint();
		if (loc != null){
			int x = loc.getBlockX();
	    	int y = loc.getBlockY();
	    	int z = loc.getBlockZ();
	    	//float yaw = loc.getYaw();
	    	//float pitch = loc.getPitch();
	    	fileDB.getNode(rname,"tppoint").setValue(x+","+y+","+z/*+","+yaw+","+pitch*/);
		} else {
			fileDB.getNode(rname,"tppoint").setValue("");
		}  
		return fileDB;
	}
	
	public static Region loadRegion(CommentedConfigurationNode region, String rname, String world) throws ObjectMappingException {		
		int maxX = region.getNode(rname,"maxX").getInt();
		int maxZ = region.getNode(rname,"maxZ").getInt();
		int minX = region.getNode(rname,"minX").getInt();
		int minZ = region.getNode(rname,"minZ").getInt();
    	int maxY = region.getNode(rname,"maxY").getInt(255);
    	int minY = region.getNode(rname,"minY").getInt(0);
    	
    	LinkedList<String> leaders = new LinkedList<String>();
    	leaders.addAll(region.getNode(rname,"leaders").getList(TypeToken.of(String.class)));
    	
    	LinkedList<String> admins = new LinkedList<String>();
    	admins.addAll(region.getNode(rname,"admins").getList(TypeToken.of(String.class)));
    	
    	LinkedList<String> members = new LinkedList<String>();
    	members.addAll(region.getNode(rname,"members").getList(TypeToken.of(String.class)));
    	    	  
    	//compatibility ------>        
        if (region.getNode(rname,"creator").getValue() != null){
        	String creator = region.getNode(rname,"creator").getString();        	  
        	if (region.getNode(rname,"owners").getValue() != null){
            	leaders.addAll(region.getNode(rname,"owners").getList(TypeToken.of(String.class)));            	               	
            }
        	if (!leaders.contains(creator)){
        		leaders.add(creator);
        	}
        	region.getNode(rname).removeChild("creator");
        	region.getNode(rname).removeChild("owners");
        } 
        //compatibility <------
        
    	String welcome = region.getNode(rname,"welcome").getString();
    	int prior = region.getNode(rname,"priority").getInt();
    	String date = region.getNode(rname,"lastvisit").getString();
    	long value = region.getNode(rname,"value").getLong();
    	boolean candel = region.getNode(rname,"candelete").getBoolean(true);
    	String rent = region.getNode(rname,"rent").getString("");
    	
    	Location<World> tppoint = null;
        if (!region.getNode(rname,"tppoint").getString().equalsIgnoreCase("")){
        	String tpstring[] = region.getNode(rname,"tppoint").getString().split(",");
            tppoint = new Location<World>(Sponge.getServer().getWorld(world).get(), Double.parseDouble(tpstring[0]), Double.parseDouble(tpstring[1]), Double.parseDouble(tpstring[2]));
        }
            	    	
  	    Region newr = new Region(rname, admins, members, leaders, new int[] {minX,minX,maxX,maxX}, new int[] {minZ,minZ,maxZ,maxZ}, minY, maxY, prior, world, date, RedProtect.cfgs.getDefFlagsValues(), welcome, value, tppoint, candel);
  	    if (rent.split(":").length >= 3){
  	    	newr.setRentString(rent);
  		}
  	    
  	    for (String flag:RedProtect.cfgs.getDefFlags()){
    		if (region.getNode(rname,"flags",flag) != null){
  			    newr.flags.put(flag,region.getNode(rname,"flags",flag).getBoolean()); 
  		    } else {
  			    newr.flags.put(flag,RedProtect.cfgs.getDefFlagsValues().get(flag)); 
  		    }    	    		
  	    } 
    	for (String flag:RedProtect.cfgs.AdminFlags){
    		if (region.getNode(rname,"flags",flag).getString() != null){
    			newr.flags.put(flag,region.getNode(rname,"flags",flag).getValue());
    		}
    	}
    	return newr;
	}
	
	public static int SingleToFiles() {
		int saved = 0;
		for (World w:Sponge.getServer().getWorlds()){
			Set<Region> regions = RedProtect.rm.getRegionsByWorld(w);			
			for (Region r:regions){
				File wf = new File(RedProtect.configDir+"data", w.getName()+File.separator+r.getName()+".conf");  
				ConfigurationLoader<CommentedConfigurationNode> regionManager = HoconConfigurationLoader.builder().setPath(wf.toPath()).build();
            	CommentedConfigurationNode fileDB = regionManager.createEmptyNode();
				
				File f = new File(RedProtect.configDir+"data", w.getName());
        		if (!f.exists()){
        			f.mkdir();
        		}
        		
    			saved++;
    			saveConf(addProps(fileDB, r), regionManager);    			
			} 
			
			File oldf = new File(RedProtect.configDir+"data", "data_"+ w.getName() + ".conf");
			if (oldf.exists()){
				oldf.delete();
			}
		}

		if (!RedProtect.cfgs.getBool("flat-file.region-per-file")){
			RedProtect.cfgs.setConfig("flat-file.region-per-file", true);
		}
		RedProtect.cfgs.save();
		return saved;
	}
	
	public static int FilesToSingle() {
		int saved = 0;		
		for (World w:Sponge.getServer().getWorlds()){
			File f = new File(RedProtect.configDir+"data", "data_" + w.getName() + ".conf");	
			Set<Region> regions = RedProtect.rm.getRegionsByWorld(w);	 
			ConfigurationLoader<CommentedConfigurationNode> regionManager = HoconConfigurationLoader.builder().setPath(f.toPath()).build();
        	CommentedConfigurationNode fileDB = regionManager.createEmptyNode();
			for (Region r:regions){
				addProps(fileDB, r);
				saved++;
				File oldf = new File(RedProtect.configDir+"data", w.getName()+File.separator+r.getName()+".conf");
				if (oldf.exists()){
					oldf.delete();
				}
			}
			File oldf = new File(RedProtect.configDir+"data", w.getName());
			if (oldf.exists()){
				oldf.delete();
			}
			saveConf(fileDB, regionManager);			
		}
		if (RedProtect.cfgs.getBool("flat-file.region-per-file")){
			RedProtect.cfgs.setConfig("flat-file.region-per-file", false);
		}
		RedProtect.cfgs.save();
		return saved;
	}
	
	private static void saveConf(CommentedConfigurationNode fileDB, ConfigurationLoader<CommentedConfigurationNode> regionManager){
    	try {			
			regionManager.save(fileDB);			
		} catch (IOException e) {
			e.printStackTrace();
		} 
    }
	
	public static boolean canBuildNear(Player p, Location<World> loc) {
		if (RedProtect.cfgs.getInt("region-settings.deny-build-near") == 0){
			return true;
		}
		int x = loc.getBlockX();
		int y = loc.getBlockY();
		int z = loc.getBlockZ();
		int radius = RedProtect.cfgs.getInt("region-settings.deny-build-near");
		
		for (int ix = x-radius; ix <= x+radius; ++ix) {
			for (int iy = y-radius; iy <= y+radius; ++iy) {
				for (int iz = z-radius; iz <= z+radius; ++iz) {
					Region reg = RedProtect.rm.getTopRegion(new Location<World>(p.getWorld(),ix, iy, iz));
					if (reg != null && !reg.canBuild(p)){
						RPLang.sendMessage(p, RPLang.get("blocklistener.cantbuild.nearrp").replace("{distance}", ""+radius));
						return false;
					}
				}
            }
		}		
		return true;
	}
	
	public static String regionNameConfiorm(String regionName, Player p){		
		String pRName = RPUtil.UUIDtoPlayer(p.getName());
		if (regionName.equals("")) {
            int i = 0;            
            regionName = RPUtil.StripName(pRName)+"_"+0;            
            while (RedProtect.rm.getRegion(regionName, p.getWorld()) != null) {
            	++i;
            	regionName = RPUtil.StripName(pRName)+"_"+i;   
            }            
            if (regionName.length() > 16) {
            	RPLang.sendMessage(p, "regionbuilder.autoname.error");
                return null;
            }
        }
        if (regionName.contains("@")) {
            p.sendMessage(toText(RPLang.get("regionbuilder.regionname.invalid.charac").replace("{charac}", "@")));
            return null;
        }
        
        //region name conform
        regionName = regionName.replace("/", "|");        
        if (RedProtect.rm.getRegion(regionName, p.getWorld()) != null) {
        	RPLang.sendMessage(p, "regionbuilder.regionname.existis");
            return null;
        }
        if (regionName.length() < 3 || regionName.length() > 16) {
        	RPLang.sendMessage(p, "regionbuilder.regionname.invalid");
            return null;
        }
        
        return regionName;
	}	
}
