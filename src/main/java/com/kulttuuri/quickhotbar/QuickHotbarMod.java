package com.kulttuuri.quickhotbar;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import cpw.mods.fml.common.FMLCommonHandler;

import com.kulttuuri.quickhotbar.proxy.IProxy;
import com.kulttuuri.quickhotbar.settings.SettingsClient;
import com.kulttuuri.quickhotbar.settings.SettingsServer;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = QuickHotbarModInfo.MODID, version = QuickHotbarModInfo.VERSION)
@NetworkMod(clientSideRequired=false, serverSideRequired=true, channels={"invpacket", "invpserv"}, packetHandler = QuickHotbarMod.class)
public class QuickHotbarMod implements IPacketHandler
{
	private static final int ITEMS_IN_ROW = 9;

    @Instance(QuickHotbarModInfo.MODID)
    public static QuickHotbarMod instance;
    
    public static SettingsClient clientSettings = new SettingsClient();
    public static SettingsServer serverSettings = new SettingsServer();
    
    @SidedProxy(clientSide = "com.kulttuuri.quickhotbar.proxy.clientProxy", serverSide = "com.kulttuuri.quickhotbar.proxy.serverProxy")
    public static IProxy proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
    	if (event.getSide() == Side.CLIENT)
    	{
    		clientSettings.loadSettingsFromFile(event.getSuggestedConfigurationFile());
        }
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
    	proxy.registerEvents();
    }

	@Override
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player)
	{
        //System.out.println("Handling custom packet: " + packet.channel);
        if (packet.channel.equals("invpserv") && FMLCommonHandler.instance().getSide() == Side.CLIENT)
        {
            //System.out.println("ENABLING SERVERSIDE SUPPORT!");
            clientSettings.handleInventorySwitchInServer = true;
        }
		if (packet.channel.equals("invpacket"))
		{
			DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(packet.data));
			boolean goingUp = false;
            boolean changeRow = false;

			try
            {
				goingUp = inputStream.readBoolean();
                changeRow = inputStream.readBoolean();
			}
            catch (IOException e1)
            {
				e1.printStackTrace();
			}

			try
			{
				InventoryPlayer playerInventory = ((EntityPlayerMP)player).inventory;
                int currentItem = playerInventory.currentItem;

				ItemStack[][] items = new ItemStack[4][9];
				items[0] = getItemsInRow(0, playerInventory);
				items[1] = getItemsInRow(1, playerInventory);
				items[2] = getItemsInRow(2, playerInventory);
				items[3] = getItemsInRow(3, playerInventory);
				
				if (goingUp)
				{
                    if (changeRow)
                    {
                        setItemsForRow(0, items[1], playerInventory);
                        setItemsForRow(1, items[2], playerInventory);
                        setItemsForRow(2, items[3], playerInventory);
                        setItemsForRow(3, items[0], playerInventory);
                    }
                    else
                    {
                        setItemForRowSlot(1, currentItem, items[2][currentItem], playerInventory);
                        setItemForRowSlot(2, currentItem, items[3][currentItem], playerInventory);
                        setItemForRowSlot(3, currentItem, items[0][currentItem], playerInventory);
                        setItemForRowSlot(0, currentItem, items[1][currentItem], playerInventory);
                    }
				}
				else
				{
                    if (changeRow)
                    {
                        setItemsForRow(0, items[3], playerInventory);
                        setItemsForRow(1, items[0], playerInventory);
                        setItemsForRow(2, items[1], playerInventory);
                        setItemsForRow(3, items[2], playerInventory);
                    }
                    else
                    {
                        setItemForRowSlot(1, currentItem, items[0][currentItem], playerInventory);
                        setItemForRowSlot(2, currentItem, items[1][currentItem], playerInventory);
                        setItemForRowSlot(3, currentItem, items[2][currentItem], playerInventory);
                        setItemForRowSlot(0, currentItem, items[3][currentItem], playerInventory);
                    }
                }
				
				playerInventory.inventoryChanged = true;
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	private ItemStack[] getItemsInRow(int row, InventoryPlayer inventory) throws Exception
	{
		ItemStack[] items = new ItemStack[9];
    	for (int i = 0; i < 9; i++)
    	{
    		items[i] = inventory.getStackInSlot(i + (row * ITEMS_IN_ROW));
    	}
    	return items;
	}
	
	private void setItemsForRow(int row, ItemStack[] items, InventoryPlayer inventory) throws Exception
	{
    	for (int i = 0; i < 9; i++)
    	{
    		inventory.setInventorySlotContents(i + (row * ITEMS_IN_ROW), items[i]);
    	}
	}

    private void setItemForRowSlot(int row, int slot, ItemStack item, InventoryPlayer inventory) throws Exception
    {
        inventory.setInventorySlotContents(row * ITEMS_IN_ROW + slot, item);
    }
}