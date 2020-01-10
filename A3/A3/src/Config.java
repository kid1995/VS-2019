
public class Config
{

	public static final int SLOTS_IN_FRAME = 25;
	public static final long FRAME_TIME = 1000;
	public static final long SLOT_TIME = FRAME_TIME / SLOTS_IN_FRAME;

	public static final int DATA_SIZE = 24;
	
	public static final int PACKET_LENGTH = 34;
	public static final int PACKET_CLASS_OFFSET = 0;
	public static final int PACKET_DATA_OFFSET = 1;
	public static final int PACKET_SLOT_OFFSET = 25;
	public static final int PACKET_TIMESTAMP_OFFSET = 26;
}
