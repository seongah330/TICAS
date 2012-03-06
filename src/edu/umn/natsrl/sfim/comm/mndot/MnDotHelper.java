/*
 * Copyright (C) 2011 NATSRL @ UMD (University Minnesota Duluth, US) and
 * Software and System Laboratory @ KNU (Kangwon National University, Korea) 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.umn.natsrl.sfim.comm.mndot;

import edu.umn.natsrl.sfim.SFIMConfig;
import java.nio.ByteBuffer;

import java.util.Arrays;

/**
 *
 * @author Chongmyung Park (chongmyung.park@gmail.com)
 */
public class MnDotHelper {  
    
    public int getMemoryAddress(byte[] buf) {
        return ByteBuffer.wrap(buf, SFIMConfig.OFF_ADDRESS, 2).getShort();
    }
    
    public int getDrop(byte[] packet) {
        return (packet[SFIMConfig.OFF_DROP_CAT] & 0xFF) >> 3;
    }

    public int getCat(byte[] packet) {
        return packet[SFIMConfig.OFF_DROP_CAT] & 0x07; // lower 3 bits
    }

    public int getLength(byte[] packet) {
        return packet[SFIMConfig.OFF_LENGTH];
    }
    
    /**
     * Returns check sum from packet
     * @param packet
     * @return 
     */
    public int getChecksum(byte[] packet) {
        return packet[packet.length - 1];
    }
    
    /**
     * Returns calculated check sum
     * @param packet
     * @return 
     */
    public byte checkSum(byte[] packet) {
        byte xsum = 0;
        for (int i = 0; i < packet.length - 1; i++) {
            xsum ^= packet[i];
        }
        return xsum;
    }

    public byte get170ControllerDropCat(byte[] req) {
        byte dropStat = (byte) (req[SFIMConfig.OFF_DROP_CAT] & 0xF8); // retrieve just drop (high 5bit)                
        // stat is always OK in simulation
        dropStat = (byte) (dropStat | SFIMConfig.STAT_OK);
        return dropStat;
    }    
    
    public int decodeBCD(byte b) {
        return decodeBCD(new byte[]{b});
    }    
    
    public int decodeBCD(byte[] b, int from, int to) {
        byte[] data = new byte[to-from+1];
        for(int i=from; i<=to; i++) data[i-from] = b[i];
        return decodeBCD(data);
    }

    public int decodeBCD(byte[] b) {
        StringBuffer buf = new StringBuffer(b.length * 2);
        for (int i = 0; i < b.length; ++i) {
            buf.append((char) (((b[i] & 0xf0) >> 4) + '0'));
            if ((i != b.length) && ((b[i] & 0xf) != 0x0A)) // if not pad char
            {
                buf.append((char) ((b[i] & 0x0f) + '0'));
            }
        }
        return Integer.parseInt(buf.toString());
    }
    
    public int getShort(byte[] b)
    {
        if(b.length != 2) return -1;        
        return ByteBuffer.wrap(b).getShort();
    }

    public void printResPacket(byte[] buf, int type) {
    }

//    public void printReqPacket(byte[] buf) {
//        System.out.print("[Requested]" + "DROP:" + getDrop(buf) + "  CAT:" + getStat(buf) + "  P_LENGTH:" + getLength(buf));
//        System.out.print(" Payload:");
//        for (int i = 0; i < getLength(buf); i++) {
//            System.out.print(buf[SFIMConfig.OFF_PAYLOAD + i] + " ");
//        }
//        System.out.println(" CHECKSUM:" + getChecksum(buf));
//    }

//    public void printResPacket(byte[] buf, int type) {
//        System.out.print("DROP:" + getDrop(buf) + "  STAT:" + getCat(buf) + "  P_LENGTH:" + getLength(buf) + "[");
//        switch (type) {
//            case SFIMConfig.DATA_BUFFER_30_SECOND:
//                System.out.print("VOLUME:");
//                for (int i = 0; i < 24; i++) {
//                    System.out.print(buf[SFIMConfig.OFF_PAYLOAD + i] + " ");
//                }
//                System.out.print("SCAN:");
//                for (int i = 24; i < 72; i += 2) {
//                    short scan = 0;
//                    scan = parseByteToShort(buf[SFIMConfig.OFF_PAYLOAD + i], buf[SFIMConfig.OFF_PAYLOAD + i + 1]);
//                    System.out.print(scan + " ");
//                }
//                break;
//            case SFIMConfig.SEND_NEXT_RECORD:
//                System.out.print("Y:" + buf[SFIMConfig.OFF_PAYLOAD]);
//                System.out.print(" M:" + buf[SFIMConfig.OFF_PAYLOAD + 1]);
//                System.out.print(" D:" + buf[SFIMConfig.OFF_PAYLOAD + 2]);
//                System.out.print(" H:" + buf[SFIMConfig.OFF_PAYLOAD + 3]);
//                System.out.print(" M:" + buf[SFIMConfig.OFF_PAYLOAD + 4]);
//                System.out.print("VOLUME:");
//                for (int i = 0; i < 24; i++) {
//                    System.out.print(buf[SFIMConfig.OFF_PAYLOAD + 5 + i] + " ");
//                }
//                System.out.print("SCAN:");
//                for (int i = 24; i < 72; i += 2) {
//                    short scan = 0;
//                    scan = parseByteToShort(buf[SFIMConfig.OFF_PAYLOAD + 5 + i], buf[SFIMConfig.OFF_PAYLOAD + 5 + i + 1]);
//                    System.out.print(scan + " ");
//                }
//                System.out.print("RAMP1:" + buf[buf.length - 4]);
//                System.out.print("RAMP2:" + buf[buf.length - 3]);
//                System.out.print("EMPTY:" + buf[buf.length - 2]);
//                break;
//            case SFIMConfig.DELETE_OLDEST_RECORD:
//                System.out.print("COUNT:" + buf[SFIMConfig.OFF_PAYLOAD]);
//                break;
//            case SFIMConfig.RAMP_METER_DATA + SFIMConfig.OFF_STATUS:
//                System.out.print("RAMP1:");
//                for (int i = 0; i < SFIMConfig.OFF_METER_2; i++) {
//                    System.out.print(buf[SFIMConfig.OFF_PAYLOAD + i] + " ");
//                }
//                System.out.print(" RAMP2:");
//                for (int i = 0; i < SFIMConfig.OFF_METER_2; i++) {
//                    System.out.print(buf[SFIMConfig.OFF_PAYLOAD + SFIMConfig.OFF_METER_2 + i] + " ");
//                }
//                break;
//            default:
//                for (int i = 0; i < buf[SFIMConfig.OFF_LENGTH]; i++) {
//                    System.out.print(buf[SFIMConfig.OFF_PAYLOAD + i] + " ");
//                }
//        }
//
//        System.out.println("]  " + "CHECKSUM:" + getChecksum(buf));
//    }
    
    
//    public short parseByteToShort(byte f, byte l) {
//        short output = 0;
//
//        output = f;
//        output <<= 8;
//        output += l;
//
//        return output;
//    }


//    public int[] get16BitBCD(int i) throws IOException {
//        if (i < 0 || i > 9999) {
//            throw new NumberFormatException("Invalid 16-bit BCD: " + i);
//        }
//        int[] data = new int[2];
//        data[0] = (digit4(i) << 4 | digit3(i));
//        data[1] = (digit2(i) << 4 | digit1(i));
//        return data;
//    }

    /** Write the specified integer to the output stream as an
     * 8-bit BCD.  Note: the valid range for an 8-bit BCD
     * value is 0 to 99. */
//    public int get8BitBCD(int i) throws IOException {
//        if (i < 0 || i > 99) {
//            throw new NumberFormatException("Invalid 8-bit BCD: " + i);
//        }
//        return (digit2(i) << 4 | digit1(i));
//    }

//    public int digit1fromBCD(int i) {
//        i &= DIGIT_MASK;
//        if (i < 0 || i > 9) {
//            throw new NumberFormatException("Invalid BCD: " + i);
//        } else {
//            return i;
//        }
//    }
//
//    public int digit2fromBCD(int i) {
//        return digit1(i >> 4);
//    }
//
//    public int get8BitfromBCD(int i) throws IOException {
//        return 10 * digit2fromBCD(i) + digit1fromBCD(i);
//    }
//
//    public int get16BitfromBCD(int hi, int lo) throws IOException {
//        return 1000 * digit2fromBCD(hi) + 100 * digit1fromBCD(hi) + 10 * digit2fromBCD(lo) + digit1fromBCD(lo);
//    }

//    /** Get the first digit (from the right) */
//    public int digit1(int i) {
//        return i % 10;
//    }
//
//    /** Get the second digit (from the right) */
//    public int digit2(int i) {
//        return (i / 10) % 10;
//    }
//
//    /** Get the third digit (from the right) */
//    public int digit3(int i) {
//        return (i / 100) % 10;
//    }
//
//    /** Get the fourth digit (from the right) */
//    public int digit4(int i) {
//        return (i / 1000) % 10;
//    }
}
