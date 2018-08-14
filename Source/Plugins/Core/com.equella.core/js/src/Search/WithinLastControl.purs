module Search.WithinLastControl where 

import Prelude

import Data.Argonaut (Json, _Number)
import Data.Array as Array
import Data.DateTime.Instant (instant, toDateTime, unInstant)
import Data.Either (fromRight)
import Data.Foldable (find)
import Data.Formatter.DateTime (Formatter, format, parseFormatString)
import Data.Lens (_Just, preview, set)
import Data.Lens.At (at)
import Data.Maybe (Maybe(..), fromMaybe)
import Data.Newtype (unwrap)
import Data.Time.Duration (class Duration, Days(..), Milliseconds(..), fromDuration)
import Data.Tuple (Tuple(..))
import EQUELLA.Environment (prepLangStrings)
import Effect.Now (now)
import Effect.Unsafe (unsafePerformEffect)
import MaterialUI.FormControl (formControl_)
import MaterialUI.Icon (icon_)
import MaterialUI.InputLabel (inputLabel_)
import MaterialUI.MenuItem (menuItem)
import MaterialUI.Properties (className, mkProp, onChange)
import MaterialUI.Select (select, value)
import MaterialUI.Styles (withStyles)
import Partial.Unsafe (unsafePartial)
import React (statelessComponent, unsafeCreateLeafElement)
import React.DOM (em', text)
import Search.SearchControl (Chip(..), Placement(..), SearchControl)
import Search.SearchQuery (QueryParam, _data, _params, emptyQueryParam, singleParam)
import SearchFilters (filterSection)
import Utils.UI (valueChange)

type AgoEntry = {name::String, emmed::Boolean, duration::Milliseconds}

dateFormat :: Formatter
dateFormat = unsafePartial $ fromRight $ parseFormatString "YYYY-MM-DDTHH:mm:ss"


beforeLast :: Number -> {data::Json, value::QueryParam}
beforeLast 0.0 = emptyQueryParam 0.0
beforeLast ms = 
  let nowInst = unsafePerformEffect now
      beforeStr = format dateFormat $ toDateTime $ fromMaybe nowInst $ instant $ 
              Milliseconds $ unwrap (unInstant nowInst) - ms
  in singleParam ms "modifiedAfter" beforeStr

milliToAgo :: Milliseconds -> String
milliToAgo s = fromMaybe "" $ _.name <$> find (_.duration >>> eq s) agoEntries

ago :: forall a. Duration a => String -> a -> AgoEntry
ago name d = {name, emmed:false, duration: fromDuration d}

agoEntries :: Array AgoEntry
agoEntries = let s = string.filterLast in [
  (ago s.none (Milliseconds 0.0)) {emmed=true},
  ago s.day (Days 1.0), 
  ago s.week (Days 7.0),
  ago s.month (Days 28.0),
  ago s.year (Days 365.0),
  ago s.fiveyear (Days $ 365.0 * 5.0)
]

withinLastControl :: SearchControl
withinLastControl =
  let 
    agoItem {name,emmed,duration:(Milliseconds ms)} = menuItem [mkProp "value" ms] $ 
        (if emmed then pure <<< em' else identity) [text name]
    _modifiedLast = at "modifiedLast"
    agoMs query = preview (_modifiedLast <<< _Just <<< _data <<< _Number) query.params
    updateMs updateQuery ms = updateQuery $ set (_params <<< _modifiedLast) $ Just $ beforeLast ms
    render {classes,query,updateQuery} = 
        filterSection {
          name:string.filterLast.name, 
          icon: icon_ [text "calendar_today"]
        } [ 
          formControl_ [
            inputLabel_ [text string.filterLast.label],
            select [ className classes.selectFilter, 
              value $ fromMaybe 0.0 $ agoMs query,
              onChange $ valueChange $ updateMs updateQuery
            ] $ (agoItem <$> agoEntries)
          ]
        ]

    withinClass = withStyles styles $ statelessComponent render
  in \{query,updateQuery} -> pure {
    render: [Tuple Filters $ unsafeCreateLeafElement withinClass {query,updateQuery}],
    chips: Array.fromFoldable $ agoMs query >>= case _ of 
      0.0 -> Nothing 
      ms -> Just $ Chip { label: string.filterLast.chip <> milliToAgo (Milliseconds ms), 
                          onDelete: updateMs updateQuery 0.0}
  }
  where 
  styles theme = {
    selectFilter: {
      width: "15em"
    }
  }

rawStrings :: { prefix :: String
, strings :: { filterLast :: { label :: String
                             , chip :: String
                             , name :: String
                             , none :: String
                             , month :: String
                             , year :: String
                             , fiveyear :: String
                             , week :: String
                             , day :: String
                             }
             }
}
rawStrings = {prefix: "searchpage", 
  strings: {
    filterLast: {
      label: "Modified within last",
      chip: "Modified within: ",
      name: "Modification date",
      none: "\xa0-\xa0",
      month: "Month",
      year: "Year",
      fiveyear: "Five years",
      week: "Week",
      day: "Day"
    }
  }
}

string :: { filterLast :: { label :: String
                , chip :: String
                , name :: String
                , none :: String
                , month :: String
                , year :: String
                , fiveyear :: String
                , week :: String
                , day :: String
                }
}
string = prepLangStrings rawStrings