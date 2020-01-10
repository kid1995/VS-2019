import java.io.IOException;
import java.io.InputStream;
import java.util.Queue;
import java.util.LinkedList;


public class DataReader implements Runnable
{

	private Queue<String> dataBuffer;
	private InputStream inputStream;
	private int readBytes;
	
	public DataReader(InputStream in, int dataSize)
	{
		dataBuffer = new LinkedList<String>();
		inputStream = in;
		readBytes = dataSize;
	}

	@Override
	public void run()
	{
		byte[] data = new byte[readBytes];
		while(true)
		{
			try
			{
				inputStream.read(data, 0, readBytes);
			} catch (IOException e)
			{
				e.printStackTrace();
			}
			dataBuffer.add(new String(data));
		}
	}
	
	public String getString()
	{
		return dataBuffer.poll();
		
	}
	
	public boolean isEmpty()
	{
		return dataBuffer.isEmpty();
	}
}
