import org.bouncycastle.crypto.engines.Salsa20Engine;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import java.util.Arrays;

/*
 * StreamChunker
 *
 * Initialize with a key (24 bytes)
 *
 * Call getChunk() repeatedly to fetch 12-byte chunks of data for our stream.
 *
 */
public class StreamChunker {
  private Salsa20Engine engine;
  private static final int blockSize = 12;
  private static byte[] zeros;

  public StreamChunker() {
    // silly default values. don't use.
    this("0123456789ABCDEF01234567".getBytes());
  }

  public StreamChunker(byte[] input) {
    this.engine = new Salsa20Engine();
    byte[] key = getKey(input);
    byte[] iv = getIV(input);
    this.engine.init(true, new ParametersWithIV(new KeyParameter(key), iv));
    this.zeros = new byte[this.blockSize];
    Arrays.fill(this.zeros, (byte) 0);
  }

  public byte[] getChunk() {
    byte[] out = new byte[this.blockSize];
    engine.processBytes(zeros, 0, this.blockSize, out, 0);
    return out;
  }

  private static byte[] getKey(byte[] input) {
    return Arrays.copyOfRange(input, 0, 16);
  }

  private static byte[] getIV(byte[] input) {
    return Arrays.copyOfRange(input, 16, 24);
  }

}
