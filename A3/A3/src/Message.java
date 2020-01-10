public class Message
{


	private byte[] packet;

	private long receiveTimestamp;
	private int receiveSlot;

	public Message(byte stationClass, byte[] data, byte nextSlot, long timestamp)
	{
		packet = new byte[Config.PACKET_LENGTH];

		packet[Config.PACKET_CLASS_OFFSET] = stationClass;

		// fill data into result array between PAKET_DATA_OFFSET and
		// PAKET_NEXT_SLOT_OFFSET
		for (int i = 0; (i < data.length) && (i < (Config.PACKET_SLOT_OFFSET - Config.PACKET_DATA_OFFSET)); i++)
		{
			packet[Config.PACKET_DATA_OFFSET + i] = data[i];
		}

		packet[Config.PACKET_SLOT_OFFSET] = (byte) (nextSlot + 1);

		// fill timestamp into result array big endian format
		setTimestamp(timestamp);
	}

	public Message(byte[] paket, long t, int s)
	{
		packet = paket.clone();
		receiveTimestamp = t;
		receiveSlot = s;
	}

	public long getReceiveTimestamp()
	{
		return receiveTimestamp;
	}
	
	public int getReceiveSlot()
	{
		return receiveSlot;
	}

	public byte[] getPaket()
	{
		return packet;
	}

	public byte getType()
	{
		return packet[Config.PACKET_CLASS_OFFSET];
	}

	public String getData()
	{
		return new String(packet, Config.PACKET_DATA_OFFSET, Config.PACKET_SLOT_OFFSET - Config.PACKET_DATA_OFFSET);
	}

	public byte getClaimedSlot()
	{
		return (byte) (packet[Config.PACKET_SLOT_OFFSET] - 1);
	}

	public long getTimestamp()
	{
		
		return byteArrayToLong(packet, Config.PACKET_TIMESTAMP_OFFSET);
	}

	public void setTimestamp(long timestamp)
	{
		longToByteArray(timestamp, packet, Config.PACKET_TIMESTAMP_OFFSET);
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();

		builder.append("Type:\t\t" + (char) getType() + "\n");
		builder.append("Data:\t\t" + getData() + "\n");
		builder.append("Slot:\t\t" + getClaimedSlot() + "\n");
		builder.append("Timestamp:\t" + getTimestamp() + "\n");

		return builder.toString();
	}

	private static long byteArrayToLong(byte[] b, int offset)
	{
		return (((long) b[offset + 7]) & 0xFF) + ((((long) b[offset + 6]) & 0xFF) << 8)
				+ ((((long) b[offset + 5]) & 0xFF) << 16) + ((((long) b[offset + 4]) & 0xFF) << 24)
				+ ((((long) b[offset + 3]) & 0xFF) << 32) + ((((long) b[offset + 2]) & 0xFF) << 40)
				+ ((((long) b[offset + 1]) & 0xFF) << 48) + ((((long) b[offset + 0]) & 0xFF) << 56);
	}

	private static void longToByteArray(long l, byte[] array, int offset)
	{
		array[offset + 0] = (byte) (l >> 56);
		array[offset + 1] = (byte) (l >> 48);
		array[offset + 2] = (byte) (l >> 40);
		array[offset + 3] = (byte) (l >> 32);
		array[offset + 4] = (byte) (l >> 24);
		array[offset + 5] = (byte) (l >> 16);
		array[offset + 6] = (byte) (l >> 8);
		array[offset + 7] = (byte) (l >> 0);

	}

}
