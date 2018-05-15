using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.IO;

namespace Lab3
{
    class ReverseWav
    {

        const int _bitsPerByte = 8;
        static int _bytesPerSample;
        public byte[] reverse(byte[] input)
        {
            getWavMetadata(input);
            int startIndexOfDataChunk = getStartIndexOfDataChunk(input);
            byte[] reversedWavFileStreamByteArray = populateReversedWavFileByteArray(input, startIndexOfDataChunk, _bytesPerSample);
            return reversedWavFileStreamByteArray;
        }
        private static void getWavMetadata(byte[] forwardsWavFileStreamByteArray)
        {
            MetadataGatherer.GetRiffText(forwardsWavFileStreamByteArray);
            MetadataGatherer.GetFileSize(forwardsWavFileStreamByteArray);
            MetadataGatherer.GetWaveText(forwardsWavFileStreamByteArray);
            MetadataGatherer.GetFmtText(forwardsWavFileStreamByteArray);
            MetadataGatherer.GetLengthOfFormatData(forwardsWavFileStreamByteArray);
            MetadataGatherer.GetTypeOfFormat(forwardsWavFileStreamByteArray);
            MetadataGatherer.GetNumOfChannels(forwardsWavFileStreamByteArray);
            MetadataGatherer.GetSampleRate(forwardsWavFileStreamByteArray);
            MetadataGatherer.GetBytesPerSecond(forwardsWavFileStreamByteArray);
            MetadataGatherer.GetBlockAlign(forwardsWavFileStreamByteArray);
            _bytesPerSample = MetadataGatherer.GetBitsPerSample(forwardsWavFileStreamByteArray) / _bitsPerByte;
            MetadataGatherer.GetListText(forwardsWavFileStreamByteArray);
            MetadataGatherer.GetDataText(forwardsWavFileStreamByteArray);
            MetadataGatherer.GetDataSize(forwardsWavFileStreamByteArray);
        }

        private static void writeReversedWavFileByteArrayToFile(byte[] reversedWavFileStreamByteArray, string reversedWavFilePath)
        {
            FileStream reversedFileStream = new FileStream(reversedWavFilePath, FileMode.Create, FileAccess.Write, FileShare.Write);
            reversedFileStream.Write(reversedWavFileStreamByteArray, 0, reversedWavFileStreamByteArray.Length);
        }

        private static byte[] populateReversedWavFileByteArray(byte[] forwardsWavFileStreamByteArray, int startIndexOfDataChunk, int bytesPerSample)
        {
            byte[] forwardsArrayWithOnlyHeaders = createForwardsArrayWithOnlyHeaders(forwardsWavFileStreamByteArray, startIndexOfDataChunk);

            byte[] forwardsArrayWithOnlyAudioData = createForwardsArrayWithOnlyAudioData(forwardsWavFileStreamByteArray, startIndexOfDataChunk);

            byte[] reversedArrayWithOnlyAudioData = reverseTheForwardsArrayWithOnlyAudioData(bytesPerSample, forwardsArrayWithOnlyAudioData);

            byte[] reversedWavFileStreamByteArray = combineArrays(forwardsArrayWithOnlyHeaders, reversedArrayWithOnlyAudioData);

            return reversedWavFileStreamByteArray;
        }

        private static byte[] combineArrays(byte[] forwardsArrayWithOnlyHeaders, byte[] reversedArrayWithOnlyAudioData)
        {
            byte[] reversedWavFileStreamByteArray = new byte[forwardsArrayWithOnlyHeaders.Length + reversedArrayWithOnlyAudioData.Length];
            Array.Copy(forwardsArrayWithOnlyHeaders, reversedWavFileStreamByteArray, forwardsArrayWithOnlyHeaders.Length);
            Array.Copy(reversedArrayWithOnlyAudioData, 0, reversedWavFileStreamByteArray, forwardsArrayWithOnlyHeaders.Length, reversedArrayWithOnlyAudioData.Length);
            return reversedWavFileStreamByteArray;
        }

        private static byte[] reverseTheForwardsArrayWithOnlyAudioData(int bytesPerSample, byte[] forwardsArrayWithOnlyAudioData)
        {
            int length = forwardsArrayWithOnlyAudioData.Length;
            byte[] reversedArrayWithOnlyAudioData = new byte[length];

            int sampleIdentifier = 0;

            for (int i = 0; i < length; i++)
            {
                if (i != 0 && i % bytesPerSample == 0)
                {
                    sampleIdentifier += 2 * bytesPerSample;
                }
                int index = length - bytesPerSample - sampleIdentifier + i;
                reversedArrayWithOnlyAudioData[i] = forwardsArrayWithOnlyAudioData[index];
            }
            return reversedArrayWithOnlyAudioData;
        }

        private static byte[] createForwardsArrayWithOnlyAudioData(byte[] forwardsWavFileStreamByteArray, int startIndexOfDataChunk)
        {
            byte[] forwardsArrayWithOnlyAudioData = new byte[forwardsWavFileStreamByteArray.Length - startIndexOfDataChunk];
            Array.Copy(forwardsWavFileStreamByteArray, startIndexOfDataChunk, forwardsArrayWithOnlyAudioData, 0, forwardsWavFileStreamByteArray.Length - startIndexOfDataChunk);
            return forwardsArrayWithOnlyAudioData;
        }

        private static byte[] createForwardsArrayWithOnlyHeaders(byte[] forwardsWavFileStreamByteArray, int startIndexOfDataChunk)
        {
            byte[] forwardsArrayWithOnlyHeaders = new byte[startIndexOfDataChunk];
            Array.Copy(forwardsWavFileStreamByteArray, 0, forwardsArrayWithOnlyHeaders, 0, startIndexOfDataChunk);
            return forwardsArrayWithOnlyHeaders;
        }

        private static byte[] populateForwardsWavFileByteArray(string forwardsWavFilePath)
        {
            FileStream forwardsWavFileStream = new FileStream(forwardsWavFilePath, FileMode.Open, FileAccess.Read, FileShare.Read);
            byte[] forwardsWavFileStreamByteArray = new byte[forwardsWavFileStream.Length];
            forwardsWavFileStream.Read(forwardsWavFileStreamByteArray, 0, (int)forwardsWavFileStream.Length);
            return forwardsWavFileStreamByteArray;
        }

        private static int getStartIndexOfDataChunk(byte[] forwardsWavFileStreamByteArray)
        {
            int startIndexOfAudioData = 12;
            int charDAsciiDecimalCode = 100; //'d' //data is located at index 70 in my .wav file
            int charAAsciiDecimalCode = 97;  //'a'
            int charTAsciiDecimalCode = 116; //'t'

            int chunkSize;

            //find "data" in the byte array
            while (!(forwardsWavFileStreamByteArray[startIndexOfAudioData] == charDAsciiDecimalCode && forwardsWavFileStreamByteArray[startIndexOfAudioData + 1] == charAAsciiDecimalCode && forwardsWavFileStreamByteArray[startIndexOfAudioData + 2] == charTAsciiDecimalCode && forwardsWavFileStreamByteArray[startIndexOfAudioData + 3] == charAAsciiDecimalCode))
            {
                startIndexOfAudioData += 4;
                chunkSize = forwardsWavFileStreamByteArray[startIndexOfAudioData] + forwardsWavFileStreamByteArray[startIndexOfAudioData + 1] * 256 + forwardsWavFileStreamByteArray[startIndexOfAudioData + 2] * 65536 + forwardsWavFileStreamByteArray[startIndexOfAudioData + 3] * 16777216;
                startIndexOfAudioData += 4 + chunkSize;
            }
            startIndexOfAudioData += 8;
            return startIndexOfAudioData;
        }
    }
}
