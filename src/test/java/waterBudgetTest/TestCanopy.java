package waterBudgetTest;


import java.net.URISyntaxException;
import java.util.HashMap;

import org.jgrasstools.gears.io.timedependent.OmsTimeSeriesIteratorReader;
import org.jgrasstools.gears.io.timedependent.OmsTimeSeriesIteratorWriter;
import org.junit.Test;


import canopy.WaterBudgetCanopy;

public class TestCanopy{

	@Test
	public void testLinear() throws Exception {

		String startDate = "1994-01-01 00:00";
		String endDate = "1994-01-02 00:00";
		int timeStepMinutes = 60;
		String fId = "ID";
		



		String inPathToPrec = "resources/Input/rainfall.csv";
		String inPathToET ="resources/Input/ET.csv";
		//String inPathToUpTake= "resources/Output/rootZone/UpTake.csv";
		String inPathToLAI= "resources/Input/LAI.csv";		
		
		String pathToS= "resources/Output/canopy/S.csv";
		String pathTroughfall= "resources/Output/canopy/Throughfall.csv";
		String pathToET= "resources/Output/canopy/ET.csv";

		
		OmsTimeSeriesIteratorReader JReader = getTimeseriesReader(inPathToPrec, fId, startDate, endDate, timeStepMinutes);
		OmsTimeSeriesIteratorReader ETReader = getTimeseriesReader(inPathToET, fId, startDate, endDate, timeStepMinutes);
		//OmsTimeSeriesIteratorReader UpTakeReader = getTimeseriesReader(inPathToUpTake, fId, startDate, endDate, timeStepMinutes);
		OmsTimeSeriesIteratorReader LAIReader = getTimeseriesReader(inPathToLAI, fId, startDate, endDate, timeStepMinutes);

		
		OmsTimeSeriesIteratorWriter writerS = new OmsTimeSeriesIteratorWriter();
		OmsTimeSeriesIteratorWriter writerThroughfall = new OmsTimeSeriesIteratorWriter();
		OmsTimeSeriesIteratorWriter writerTranspiration = new OmsTimeSeriesIteratorWriter();


		writerS.file = pathToS;
		writerS.tStart = startDate;
		writerS.tTimestep = timeStepMinutes;
		writerS.fileNovalue="-9999";
		
		writerThroughfall.file = pathTroughfall;
		writerThroughfall.tStart = startDate;
		writerThroughfall.tTimestep = timeStepMinutes;
		writerThroughfall.fileNovalue="-9999";
		
		writerTranspiration.file = pathToET;
		writerTranspiration.tStart = startDate;
		writerTranspiration.tTimestep = timeStepMinutes;
		writerTranspiration.fileNovalue="-9999";
	
		
		WaterBudgetCanopy waterBudget= new WaterBudgetCanopy();


		while( JReader.doProcess ) {
		
			waterBudget.solver_model="dp853";
			waterBudget.ET_model="AET";
			// 0<Imax<3
			waterBudget.Imax=2;
			waterBudget.k=0.463;
			waterBudget.s_CanopyMax=0.005704;
			waterBudget.a_c=2;
			waterBudget.b_c=1;

			
			JReader.nextRecord();
			
			HashMap<Integer, double[]> id2ValueMap = JReader.outData;
			waterBudget.inHMRain = id2ValueMap;
			

            
            ETReader.nextRecord();
            id2ValueMap = ETReader.outData;
            waterBudget.inHMETp = id2ValueMap;
            
            //UpTakeReader.nextRecord();
            //id2ValueMap = UpTakeReader.outData;
            //waterBudget.inHMRootUpTake = id2ValueMap;
            
            LAIReader.fileNovalue="-9999.0";
            LAIReader.nextRecord();
            id2ValueMap = LAIReader.outData;
            waterBudget.inHMLAI = id2ValueMap;

            waterBudget.process();
            
            HashMap<Integer, double[]> outHMStorage = waterBudget.outHMStorage;
            HashMap<Integer, double[]> outHMThoroguhfall = waterBudget.outHMThroughfall;
            HashMap<Integer, double[]> outHMET = waterBudget.outHMTranspiration;
            
            
			writerS.inData = outHMStorage ;
			writerS.writeNextLine();
			
			if (pathToS != null) {
				writerS.close();
			}
			
			writerThroughfall.inData = outHMThoroguhfall;
			writerThroughfall.writeNextLine();
		
			if (pathTroughfall != null) {
				writerThroughfall.close();
			}
			
			writerTranspiration.inData = outHMET;
			writerTranspiration.writeNextLine();
			
			if (pathToET != null) {
				writerTranspiration.close();
			}
			
			

		}
		JReader.close();
        LAIReader.close();
        ETReader.close();
        //UpTakeReader.close();

	}


	private OmsTimeSeriesIteratorReader getTimeseriesReader( String inPath, String id, String startDate, String endDate,
			int timeStepMinutes ) throws URISyntaxException {
		OmsTimeSeriesIteratorReader reader = new OmsTimeSeriesIteratorReader();
		reader.file = inPath;
		reader.idfield = "ID";
		reader.tStart = startDate;
		reader.tTimestep = 60*24;
		reader.tEnd = endDate;
		reader.fileNovalue = "-9999.0";
		reader.initProcess();
		return reader;
	}
}