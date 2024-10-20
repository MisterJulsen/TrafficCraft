# Road Salt
Road salt prevents snow and ice from forming on roads, just like in real life. After you have sprinkled some salt on your road, all snow and ice in an area of up to 4 blocks will melt.

## Gathering
You can get road salt by mining salt deposits in oceans and rivers. You can increase the output with a tool enchanted with Fortune.

## Behaviour
- The salt is gradually used up when it melts snow and ice. The spot at which the salt was once sprinkled, mixes with the melted water and becomes increasingly muddier and diluted until it eventually disappears and has to be sprinkled again (can be deactivated in the config)
- The effect of the salt is influenced by the `randomTickSpeed`.
- Road salt has 3 quality levels
    - `Fresh`: Freshly spread salt with maximum effectiveness
    - `Muddy`: Salt that has already been mixed with mud and water
    - `Diluted`: Salt that is almost used up
- Fresh salt melts snow and ice faster than diluted salt. The effectiveness is reduced by half for each quality level. The same applies to the time until the next stage. Older salt lasts longer than fresher salt, due to its reduced effectiveness.
- Road salt has a damaging effect on plants and soil in the area around it (4 blocks). The soil becomes permanently infertile and plants (grass, flowers, crops, saplings, etc.) get destroyed.