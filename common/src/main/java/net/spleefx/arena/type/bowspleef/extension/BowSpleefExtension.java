package net.spleefx.arena.type.bowspleef.extension;

import lombok.Getter;
import net.spleefx.extension.MatchExtension;
import net.spleefx.extension.MatchExtension.StandardExtension;

@Getter
@StandardExtension
public class BowSpleefExtension extends MatchExtension {

    private boolean bounceArrows;
    private boolean removeTNTWhenPrimed;
    private TripleArrowsOptions tripleArrows = new TripleArrowsOptions();

}
