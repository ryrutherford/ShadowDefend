/**
 * attackable interface provides a deduct health that can be used to deduct health from any enemies
 * although there is only one type of enemy (slicers) at this point
 * I made the attackable interface in case other enemies with different characteristics than slicers are added later on
*/
public interface Attackable {
    void deductHealth(int deduction);
}
