package strategie;

public interface MemoryManagerProduct {

	public MemoryManagerProduct clone(MemoryManagerProduct object);

	public MemoryManagerProduct clone();

	public String getNom();
	
}
