package br.net.fabiozumbi12.RedProtect;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class RPEconomy {

	public static double getRegionValue(Region r){
		double regionCost = 0.0;
		World w = RedProtect.serv.getWorld(r.getWorld());
		int maxX = r.getMaxMbrX();
		int minX = r.getMinMbrX();
		int maxZ = r.getMaxMbrZ();
		int minZ = r.getMinMbrZ();
		int factor = 0;
		for(int x = minX; x < maxX; x++){
		  for(int y = 0; y < 256; y++) {
		    for(int z = minZ; z < maxZ; z++) {
		    	
		      Block b = w.getBlockAt(x,y,z);
		      if (b.isEmpty()){
		    	  continue;
		      }
		      
		      if (b.getState() instanceof InventoryHolder){
		    	  Inventory inv = ((InventoryHolder) b.getState()).getInventory();
		    	  
		    	  if (inv.getSize() == 54){		    	  
			    	  factor = 2;
			      } else {
			    	  factor = 1;
			      }
		    	  
		    	  for (ItemStack item:inv.getContents()){		    		  
		    		  if (item == null || item.getAmount() == 0){
		    			  continue;
		    		  }
		    		  regionCost = regionCost + ((RPConfig.getBlockCost(item.getType().name()) * item.getAmount()) / factor);
		    		  if (item.getEnchantments().size() > 0){
		    			  for (Enchantment enchant:item.getEnchantments().keySet()){
			    			  regionCost = regionCost + ((RPConfig.getEnchantCost(enchant.getName()) * item.getEnchantments().get(enchant)) / factor);
			    		  } 
		    		  }		    		  
		    	  }
		      }	else {
		    	  regionCost = regionCost + RPConfig.getBlockCost(b.getType().name());
		      }		      
		    }
		  }
		}
		r.setValue(regionCost);
		return regionCost;		
	}

	public static String getCostMessage(Region r){
		return RPLang.get("economy.forsale") + " &6" + RPConfig.getEcoString("economy-symbol") + r.getValue() +" &2"+ RPConfig.getEcoString("economy-name");
	}
	
	public static boolean putToSell(Region r, String uuid, Double value) {
		try {			
			r.setCreator(uuid);
			r.clearMembers();
			r.clearOwners();
			r.setValue(value);
			r.setWelcome(getCostMessage(r));			
			r.setFlag("for-sale", true);
			r.setName(RPUtil.nameGen(RPUtil.UUIDtoPlayer(uuid),r.getWorld()));
			return true;
		} catch (Exception e){
			return false;
		}		
	}
	
	public static boolean BuyRegion(Region r, String uuid) {
		try {			
			r.clearMembers();
			r.clearOwners();
			r.setCreator(uuid);
			r.addOwner(uuid);
			r.setDate(RPUtil.DateNow());
			r.setWelcome("");
			r.flags = RPConfig.getDefFlagsValues();
			r.setName(RPUtil.nameGen(RPUtil.UUIDtoPlayer(uuid),r.getWorld()));
			r.setFlag("for-sale", false);
			return true;
		} catch (Exception e){
			return false;
		}		
	}
}
