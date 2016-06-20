import com.impinj.octane.*;
import java.util.*;

public class CRFIDVirus {
	public final static int password = 0xCAFE;
	/* 7A65 C0DE ACCE 55E5 DA7A FA7E CAFE F00D */
	public final static byte[] key = { (byte) 0x7A, (byte) 0x65, (byte) 0xC0, (byte) 0xDE,
																			(byte) 0xAC, (byte) 0xCE, (byte) 0x55, (byte) 0xE5,
																			(byte) 0xDA, (byte) 0x7A, (byte) 0xFA, (byte) 0x7E,
																			(byte) 0xCA, (byte) 0xFE, (byte) 0xF0, (byte) 0x0D };

	public static void main(String args[]) {
		try {
			// Hello, World
			System.out.println("I solemnly swear I'm up to no good.");

			// Connect to Reader
			String hostname = System.getProperty("hostname");
			if (hostname == null) {
				throw new Exception("Hostname Unspecified");
			}
			ImpinjReader reader = new ImpinjReader();
			reader.connect(hostname);

			// Configure Reader
			Settings settings = reader.queryDefaultSettings();

			ReportConfig report = settings.getReport();
			report.setIncludeAntennaPortNumber(true);
			report.setMode(ReportMode.Individual);

			settings.setReaderMode(ReaderMode.AutoSetDenseReader);

			AntennaConfigGroup antennas = settings.getAntennas();
			antennas.disableAll();
			antennas.enableById(new short[]{1});
			antennas.getAntenna((short) 1).setIsMaxRxSensitivity(false);
			antennas.getAntenna((short) 1).setIsMaxTxPower(false);
			antennas.getAntenna((short) 1).setTxPowerinDbm(30.0);
			antennas.getAntenna((short) 1).setRxSensitivityinDbm(-70);

			//reader.setTagReportListener(new CRFIDVirusReportListener());

			reader.applySettings(settings);


			// Generate Secret RN16
			reader.addOpSequence(writeSequence(MemoryBank.Reserved, (short) 1, (short) 0x0, TagData.fromWord(0x0000)));

			// Fetch Secret RN16
			reader.addOpSequence(readSequence(MemoryBank.Reserved, (short) 1, (short) 2, (short) 8));

			// Set Listener for Auth Phase
			PromiseRN p = PromiseRN.get();
			reader.setTagOpCompleteListener(new AuthListener());

			// Start the Reader
			reader.start();
			System.out.println(" - Generating RN16");
			List<Integer> ct = null;
			for (int count = 0; p.isSet() == false; count++) {
				Thread.sleep(1000);
				// 10 second timeout
				if (count >= 10) {
					reader.stop();
					reader.disconnect();
					throw new Exception("Timeout Waiting for RN16");
				}
			}
			ct = p.getRN();

			// RN16 Obtained
			reader.stop();
			reader.deleteAllOpSequences();

			// Decrypt RN16
			if (ct.size() != 8) {
				reader.disconnect();
				throw new Exception(String.format("Received %d Values, Expected 8", ct.size()));
			}
			byte[] ba = new byte[16];
			for (int i = 0; i < 16; i++) {
				int v = ct.get(i >> 1);
				ba[i] = (byte)(((i % 2 == 0) ? (v >> 8) : v) & 0x00FF);
			}
			byte[] pk = AES_decrypt(key, ba);
			int rn = (int) pk[0];
			rn = (rn << 8) | (((int) pk[1]) & 0x00FF);
			rn = rn & 0x0000FFFF;
			System.out.println(String.format(" - Decrypted RN16: %04X", rn));

			// Authenticate
			int data = password ^ rn;
			reader.addOpSequence(writeSequence(MemoryBank.Reserved, (short) 1, (short) 0x2, TagData.fromWord(data)));

			// Demonstrate Protection
			reader.addOpSequence(readSequence(MemoryBank.User, (short) 1, (short) 0, (short) 4));
			reader.addOpSequence(writeSequence(MemoryBank.User, (short) 1, (short) 0, TagData.fromWord(0x7F01)));
			reader.addOpSequence(writeSequence(MemoryBank.User, (short) 1, (short) 1, TagData.fromWord(0x7F02)));
			reader.addOpSequence(writeSequence(MemoryBank.User, (short) 1, (short) 2, TagData.fromWord(0x7F03)));
			reader.addOpSequence(readSequence(MemoryBank.User, (short) 1, (short) 0, (short) 4));

			// Set Listener for Transfer Phase
			ct.add(new Integer(6));
			reader.setTagOpCompleteListener(new AccessListener());

			// Transfer Data
			reader.start();
			for (int count = 0; true; count++) {
				int acks = 0;
				synchronized (ct) {
					acks = ct.get(ct.size() - 1);
				}
				if (acks <= 0) {
					break;
				} else if (count < 10) {
					Thread.sleep(200);
				}
			}

			// Stop the Reader & Exit
			reader.stop();
			reader.disconnect();
			System.out.println("Mischief managed.");
			System.exit(0);
		} catch (Throwable t) {
			System.err.println(" ! " + t);
			System.exit(1);
		}
	}

	private static class CRFIDVirusReportListener implements TagReportListener {
		public void onTagReported(ImpinjReader ir, TagReport tr) {
			for (Tag t : tr.getTags()) {
				System.out.println(" - Found Tag: " + t.getEpc().toHexWordString());
			}
		}
	}

	private static class AccessListener extends CRFIDTagOpListener {
			private void decr() {
				List<Integer> l = null;
				try {
					l = PromiseRN.get().getRN();
				} catch (InterruptedException ie) {
					l = new ArrayList<Integer>();
					l.add(new Integer(0));
				}
				synchronized (l) {
					l.set(l.size() - 1, l.get(l.size() - 1) - 1);
				}
			}

			protected void handleTag(Tag t) {
				System.out.println(" - EPC: " + t.getEpc().toHexWordString());
			}
			protected void readSuccess(TagReadOpResult res) { 
				decr();
				System.out.println(" - Read: " + res.getData().toHexWordString());
			}
			protected void readFail(TagReadOpResult res, String msg) {
				decr();
				System.out.println(" * Read Failed " + msg);
			}
			protected void writeSuccess(TagWriteOpResult res) {
				decr();
				System.out.println(" - Write Success");
			}
			protected void writeFail(TagWriteOpResult res, String msg) {
				decr();
				System.out.println(" ! Write Failed: " + msg);
			}
			protected void postHandle() {
				System.out.println("");
			}
	}

	private static class AuthListener extends CRFIDTagOpListener {
		public AuthListener() {
			this.rn = PromiseRN.get();
		}

		private PromiseRN rn;

		protected void handleTag(Tag t) {
			System.out.println(" - EPC: " + t.getEpc().toHexWordString());
		}

		protected void readSuccess(TagReadOpResult res) {
			rn.setRN(res.getData().toWordList());
			System.out.println(" - Read RN16: " + res.getData().toHexWordString());
		}

		protected void readFail(TagReadOpResult res, String msg) {
			System.out.println(" ! Read Failed: " + msg);
		}

		protected void writeSuccess(TagWriteOpResult res) {
			System.out.println(" - New RN16 Generated");
		}

		protected void writeFail(TagWriteOpResult res, String msg) {
			System.out.println(" ! Write Failed: " + msg);
		}

		protected void postHandle() {
			System.out.println("");
		}
	}

	private static class PromiseRN {
		private static PromiseRN instance = null;

		public static PromiseRN get() {
			if (instance == null) {
				instance = new PromiseRN();
			}
			return instance;
		}

		private PromiseRN() {
			this.rn = null;
			this.set = false;
		}

		private List<Integer> rn;
		private boolean set;

		public synchronized boolean isSet() {
			return this.set;
		}

		public synchronized void unsetRN() {
			this.set = false;
		}

		public synchronized List<Integer> getRN() throws InterruptedException {
			while (this.set == false) {
				Thread.sleep(1);
			}
			return this.rn;
		}

		public synchronized void setRN(List<Integer> l) {
			this.rn = l;
			set = true;
		}
	}

	static {
		System.loadLibrary("./CRFIDVirus");
	}
	private static native byte[] AES_decrypt(byte[] key, byte[] data);

	private static TagOpSequence writeSequence(MemoryBank membank, short count, short wordptr, TagData data) {
		TagOpSequence seq = new TagOpSequence();
		seq.setOps(new ArrayList<TagOp>());
		seq.setExecutionCount(count);
		seq.setState(SequenceState.Active);

		TagWriteOp writeOp = new TagWriteOp();
		writeOp.setMemoryBank(membank);
		writeOp.setWordPointer(wordptr);
		writeOp.setData(data);
		seq.getOps().add(writeOp);

		seq.setTargetTag(null);

		return seq;
	}
	private static TagOpSequence writeSequence(MemoryBank membank, short wordptr, TagData[] data) {
		TagOpSequence seq = new TagOpSequence();
		seq.setOps(new ArrayList<TagOp>());
		seq.setExecutionCount((short)1);
		seq.setState(SequenceState.Active);
		for (short i = (short) 0; i < (short) data.length; i++) {
			TagWriteOp op = new TagWriteOp();
			op.setMemoryBank(membank);
			op.setWordPointer((short)(wordptr + i));
			op.setData(data[i]);
			seq.getOps().add(op);
		}
		seq.setTargetTag(null);
		return seq;
	}
	private static TagOpSequence readSequence(MemoryBank membank, short count, short wordptr, short words) {
		TagOpSequence seq = new TagOpSequence();
		seq.setOps(new ArrayList<TagOp>());
		seq.setExecutionCount(count);
		seq.setState(SequenceState.Active);

		TagReadOp readOp = new TagReadOp();
		readOp.setMemoryBank(membank);
		readOp.setWordPointer(wordptr);
		readOp.setWordCount(words);
		seq.getOps().add(readOp);
		
		seq.setTargetTag(null);

		return seq;
	}
}

