package enclosure.pi.monitor.thread;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.BlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jsoniter.JsonIterator;
import com.jsoniter.output.JsonStream;

import enclosure.pi.monitor.printer.PrinterHandler;
import enclosure.pi.monitor.websocket.DataType;
import enclosure.pi.monitor.websocket.SocketMessage;
import enclosure.pi.monitor.websocket.WebSocketClient;
import enclosure.pi.monitor.websocket.WsAction;
import home.websocket.WebSocketClientEndPoint.MessageHandler;

//thread that handle the serial console for the printer
public class PrinterSerialConsoleThread implements Runnable, MessageHandler{

	private static final Logger logger = LogManager.getLogger(PrinterSerialConsoleThread.class);

	private BlockingQueue<String> printQueue;
	private StringBuilder outputs;
	private int recordCnt = 0;
	private Path serialFileOutput;

	//	private int bufferCount = 0;
	private WebSocketClient wsClient;

	public PrinterSerialConsoleThread(BlockingQueue<String> queue, Path fileName) {
		logger.debug("Starting:: PrinterSerialConsoleThread");
		this.printQueue = queue;
		//		this.bufferCount = bufferCount;
		wsClient = WebSocketClient.getInstance();

		wsClient.addHandler(this);
		this.outputs = new StringBuilder();

		this.serialFileOutput = fileName;

		SocketMessage msg = new SocketMessage(WsAction.REGISTER_FOR_SERIAL,DataType.NONE, "true" );
		WebSocketClient.getInstance().sendMessage(msg);
	}

	@Override
	public void run() {

		while(true) {
			try {
				String str = "";
				try {
					str = printQueue.take();
					if(!str.contains("\n")) {
						str = str + "\n";
					}

					outputs.append(str );
					logger.debug("PrinterSerialConsoleThread: --> " + str);
				} catch (InterruptedException e) {
					break;
				}			

				//then send to host if any connected.

				//repeat.. When the user onnect to the page.. it will trigger the rest interface. then everyting will be through here.
				//have a buffer of 6000 lines.

				SocketMessage msg = new SocketMessage(WsAction.SEND_TO_SERIAL_CONSOLE,DataType.PRINTER_SERIAL_DATA_WEB, str );
				WebSocketClient.getInstance().sendMessage(msg);

				if(recordCnt == 500) {
					logger.debug("Buffer reached , writing to file");
					//write to file
					writeOutputs();
					//empty array
					outputs = new StringBuilder();
					recordCnt = 0;
				}else {
					recordCnt ++;
				}

			}catch(Exception e) {
				logger.error("Error writing in run" , e);;
			}
		}

		outputs.append("Printer shutdown");
		writeOutputs();

		logger.debug("Stopped thread PrinterSerialConsoleThread" );

	}
	private void writeOutputs() {
		try {
			Files.write(serialFileOutput, outputs.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);

		} catch (IOException e) {
			logger.error("Error writing to file" , e);;
		}
	}

	@Override
	public void handleMessage(String str) {
		logger.debug("Messages from websocket: " + str);

		try {

			SocketMessage msg = JsonIterator.deserialize(str, SocketMessage.class);
			if (msg.getAction() == WsAction.REGISTER_FOR_SERIAL && msg.getDataType() == DataType.PRINTER_SERIAL_DATA_TO_BACKEND) { //serial init. so basically all serial data that is stored

				SocketMessage returnMsg = new SocketMessage(WsAction.SEND_TO_SERIAL_CONSOLE,DataType.PRINTER_SERIAL_DATA_TO_USER, outputs.toString() );
				returnMsg.setAdditionalMessage(msg.getMessage());
				WebSocketClient.getInstance().sendMessage(JsonStream.serialize(returnMsg));
			}else if (msg.getAction() == WsAction.SEND_TO_SERIAL_CONSOLE && msg.getDataType() == DataType.PRINTER_SERIAL_DATA_TO_BACKEND) { //command coming from the web
				//TODO
				logger.debug("Msg -----------> : " + msg.getMessage());
				PrinterHandler.getInstance().sendCommand(msg.getMessage(), 0);

			}

		} catch (IOException | InterruptedException e) {
			logger.error("Error in handleMessage", e );
		}
	}

}
