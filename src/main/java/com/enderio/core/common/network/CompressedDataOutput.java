package com.enderio.core.common.network;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.zip.DeflaterOutputStream;

public class CompressedDataOutput extends DataOutputStream {

  final ByteArrayOutputStream baos;

  public CompressedDataOutput() {
    this(new ByteArrayOutputStream());
  }

  private CompressedDataOutput(ByteArrayOutputStream baos) {
    super(new BufferedOutputStream(new DeflaterOutputStream(baos)));
    this.baos = baos;
  }

  public void writeVariable(int value) throws IOException {
    int val = value;
    while ((val & ~0x7F) != 0) {
      writeByte(val | 0x80);
      val >>= 7;
    }
    writeByte(val);
  }

  public byte[] getCompressed() throws IOException {
    close();
    return baos.toByteArray();
  }
}
