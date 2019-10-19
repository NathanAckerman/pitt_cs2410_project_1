import java.util.ArrayList;
import java.io.*;

public class InstructionCache
{
	public ArrayList<Instruction> instructions;

	private final int BASE_ADDR = 1000;
	private Instruction[] cache_line;
	private int num_instr_in_left_unissued;
	public Issuer issuer;
	private Integer pc;
	public Integer next_pc;

	public InstructionCache(Issuer issuer, Integer pc)
	{
		instructions = new ArrayList<Instruction>();
		cache_line = new Instruction[4];
		num_instr_in_left_unissued = 0;
		this.issuer = null;
		this.pc = pc;
		this.next_pc = pc+1;
	}

	public String toString()
	{
		String result = "\nINSTRUCTION CACHE:\n\n";
		for (Instruction instruction: this.instructions)
			result += instruction.toString() + "\n";

		return result;
	}

	/*
	 * Given an instruction address, this function will
	 * find and return the associated Instruction object
	 */
	public Instruction findInstruction(int address)
	{
		for (Instruction instruction: this.instructions)
			if (instruction.getAddess() == address)
				return instruction;

		return null;
	}

	public void doCycle()
	{
		if (instr_left_in_line() && pc_in_line(pc)) {
			issueInstructions();
		} else {
			get_cache_line_with_pc(pc);
			issueInstructions();
		}
		if (next_pc == null) {
			pc = pc+1;
			next_pc = pc + 1;
		} else {
			pc = next_pc;
			next_pc = pc+1;
		}
	}

	private void issueInstructions()
	{
		//printCacheLine();
		int num_spots_in_issuer = issuer.getEmptySpots();
		boolean issuing = false;
		int num_sent = 0;
		for (Instruction instr : cache_line) {
			if (num_sent == num_spots_in_issuer) {
				break;
			}
			if (instr != null && instr.address == pc) {
				issuing = true;
			}
			if (issuing) {
				if (instr == null) {
					break;
				}
				if (issuer.enqueueInstruction(cloneInstruction(instr))) {
					num_sent++;
				}
				
			}
		}
	}

	private void printCacheLine()
	{
		for (Instruction instr : cache_line)
		{
			System.out.println(instr);
		}
	}

	private boolean pc_in_line(int pc)
	{
		for(Instruction instr: cache_line){
			if(instr.address == pc) {//TODO need to decide how pc references are done
				return true;
			}
		}
		return false;
	}

	private boolean instr_left_in_line()
	{
		return num_instr_in_left_unissued != 0;	
	}

	private void get_cache_line_with_pc(int pc) {
		Instruction pc_instr = findInstruction(pc);
		clear_cacheline();
	
		if ((pc-BASE_ADDR) % 4 == 0 ) {
			cache_line[0] = pc_instr;
			cache_line[1] = findInstruction(pc+1);
			cache_line[2] = findInstruction(pc+2);
			cache_line[3] = findInstruction(pc+3);
		} else if ((pc-BASE_ADDR) % 4 == 4) {
			cache_line[0] = findInstruction(pc-1);
			cache_line[1] = pc_instr;
			cache_line[2] = findInstruction(pc+1);
			cache_line[3] = findInstruction(pc+2);
		} else if ((pc-BASE_ADDR) % 4 == 8) {
			cache_line[0] = findInstruction(pc-2);
			cache_line[1] = findInstruction(pc-1);
			cache_line[2] = pc_instr;
			cache_line[3] = findInstruction(pc+1);
		} else if ((pc-BASE_ADDR) % 4 == 12) {
			cache_line[0] = findInstruction(pc-3);
			cache_line[1] = findInstruction(pc-2);
			cache_line[2] = findInstruction(pc-1);
			cache_line[3] = pc_instr;
		}
	}

	private void clear_cacheline()
	{
		for(int i = 0; i < 4; i++)
		{
			cache_line[i] = null;
		}
	}


/*
	//okay so because java is a garbage language there is no good way to deep copy an object
	private Instruction cloneInstruction(Instruction orig_instr)
	{
		Instruction new_instr = null;
		try {
			//serialize
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(orig_instr);
			oos.flush();
			oos.close();
			bos.close();
			byte[] byteData = bos.toByteArray();

			//unserialize
			ByteArrayInputStream bais = new ByteArrayInputStream(byteData);
			new_instr = (Instruction) new ObjectInputStream(bais).readObject();
		} catch (Exception e) {
			System.out.println("serialization for instr copy didn't work\n");
			System.exit(1);
		}
		return new_instr;
	}
*/
	private Instruction cloneInstruction(Instruction orig_instr)
	{
		Instruction new_instr = new Instruction();
		
		new_instr.address = orig_instr.address;
		new_instr.opcode = orig_instr.opcode;
		new_instr.source_reg1 = orig_instr.source_reg1;
		new_instr.source_reg2 = orig_instr.source_reg2;
		new_instr.dest_reg = orig_instr.dest_reg;
		new_instr.dest_reg_original_str = orig_instr.dest_reg_original_str;
		new_instr.source_reg1_original_str = orig_instr.source_reg1_original_str;
		new_instr.source_reg2_original_str = orig_instr.source_reg2_original_str;
		new_instr.immediate = orig_instr.immediate;
		new_instr.target = orig_instr.target;
		new_instr.predicted_target = orig_instr.predicted_target;
		return new_instr;
	}

}
