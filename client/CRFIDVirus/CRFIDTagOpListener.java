import com.impinj.octane.*;
import java.util.*;

public abstract class CRFIDTagOpListener implements TagOpCompleteListener {
	public void onTagOpComplete(ImpinjReader ir, TagOpReport tr) {
		for (TagOpResult res : tr.getResults()) {
			Tag t = res.getTag();
			this.handleTag(t);
			if (res instanceof TagReadOpResult) {
				TagReadOpResult read = (TagReadOpResult) res;
				this.handleRead(read);
			}
			if (res instanceof TagWriteOpResult) {
				TagWriteOpResult write = (TagWriteOpResult) res;
				this.handleWrite(write);
			}
			this.postHandle();
		}
	}

	private void handleRead(TagReadOpResult res) {
		switch (res.getResult()) {
			case Success:
				this.readSuccess(res);
				break;
			case NoResponseFromTag:
				this.readFail(res, "No Response from Tag");
				break;
			case NonspecificTagError:
				this.readFail(res, "Tag Error");
				break;
			case NonspecificReaderError:
				this.readFail(res, "Reader Error");
				break;
			default:
				this.readFail(res, "Unknown Error");
				break;
		}
	}

	private void handleWrite(TagWriteOpResult res) {
		switch (res.getResult()) {
			case Success:
				this.writeSuccess(res);
				break;
			case TagMemoryOverrunError:
				this.writeFail(res, "Tag Memory Overrun");
				break;
			case TagMemoryLockedError:
				this.writeFail(res, "Tag Memory Locked");
				break;
			case NoResponseFromTag:
				this.writeFail(res, "No Response from Tag");
				break;
			case InsufficientPower:
				this.writeFail(res, "Insufficient Power");
				break;
			case NonspecificTagError:
				this.writeFail(res, "Tag Error");
				break;
			case NonspecificReaderError:
				this.writeFail(res, "Reader Error");
				break;
			default:
				this.writeFail(res, "Unknown Error");
				break;
		}
	}

	protected abstract void handleTag(Tag t);
	protected abstract void readSuccess(TagReadOpResult res);
	protected abstract void readFail(TagReadOpResult res, String msg);
	protected abstract void writeSuccess(TagWriteOpResult res);
	protected abstract void writeFail(TagWriteOpResult res, String msg);
	protected abstract void postHandle();
}
